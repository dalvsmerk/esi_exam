package com.example.rentit.sales.application.service;

import com.example.rentit.common.application.exception.InvoiceNotSentException;
import com.example.rentit.common.application.exception.ValidationException;
import com.example.rentit.common.application.service.BusinessPeriodValidator;
import com.example.rentit.common.application.service.ExternalAPICommunicator;
import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import com.example.rentit.inventory.domain.model.PlantReservation;
import com.example.rentit.inventory.domain.model.PlantStatus;
import com.example.rentit.inventory.domain.repository.InventoryRepository;
import com.example.rentit.inventory.domain.repository.PlantInventoryItemRepository;
import com.example.rentit.inventory.domain.repository.PlantReservationRepository;
import com.example.rentit.sales.application.dto.InvoiceDTO;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import com.example.rentit.sales.application.exception.*;
import com.example.rentit.sales.domain.model.Invoice;
import com.example.rentit.sales.domain.model.InvoiceStatus;
import com.example.rentit.sales.domain.model.POStatus;
import com.example.rentit.sales.domain.model.PurchaseOrder;
import com.example.rentit.sales.domain.repository.InvoiceRepository;
import com.example.rentit.sales.domain.repository.PurchaseOrderRepository;
import com.example.rentit.support.domain.model.MaintenanceTask;
import com.example.rentit.support.domain.repository.MaintenanceTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.DataBinder;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SalesService {

    @Autowired
    PlantInventoryItemRepository plantInventoryItemRepository;

    @Autowired
    PurchaseOrderRepository poRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    PlantReservationRepository plantReservationRepository;

    @Autowired
    PurchaseOrderAssembler purchaseOrderAssembler;

    @Autowired
    MaintenanceTaskRepository maintenanceTaskRepository;

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    InvoiceAssembler invoiceAssembler;

    @Autowired
    ExternalAPICommunicator externalAPICommunicator;

    public List<PurchaseOrderDTO> findPOs() {
        List<PurchaseOrder> POs = poRepository.findAll();

        return purchaseOrderAssembler.toResources(POs);
    }

    public PurchaseOrderDTO findPO(Long id) {
        PurchaseOrder po = poRepository.findById(id).orElse(null);

        return purchaseOrderAssembler.toResource(po);
    }

    public PurchaseOrderDTO createPO(PurchaseOrderDTO poDTO) throws Exception {
        BusinessPeriod period = BusinessPeriod.of(
                poDTO.getRentalPeriod().getStartDate(),
                poDTO.getRentalPeriod().getEndDate());

        DataBinder binder = new DataBinder(period);
        binder.addValidators(new BusinessPeriodValidator());
        binder.validate();

        if (binder.getBindingResult().hasErrors()) {
            throw new ValidationException("period", binder.getBindingResult());
        }

        if (poDTO.getCustomerOrderId() == null) {
            throw new CustomerPurchaseOrderIdNotProvidedException("PurchaseOrder", "Customer purchase order Id not provided");
        }

        if (poDTO.getPlant() == null) {
            throw new PlantNotFoundException(null);
        }

        PlantInventoryItem plant = plantInventoryItemRepository.findById(poDTO.getPlant().get_id()).orElse(null);

        if (plant == null) {
            throw new PlantNotFoundException(poDTO.getPlant().get_id());
        }

        PurchaseOrder po = PurchaseOrder.of(plant, period, poDTO.getCustomerOrderId());
        po = poRepository.saveAndFlush(po);

        LocalDate startDate = po.getRentalPeriod().getStartDate();
        LocalDate endDate = po.getRentalPeriod().getEndDate();

        // 1. If not available for the period - reject
        if (!isPlantItemAvailable(plant.getId(), po.getRentalPeriod())) {
            po.setStatus(POStatus.REJECTED);

            String template = "Plant %d is not available from %s to %s";
            String message = String.format(template, plant.getId(), startDate, endDate);

            throw new PlantNotAvailableException(message);
        }

        // 2. If scheduled for maintenance - replace with another plant
        if (isPlantScheduledForMaintenance(plant.getId(), endDate)) {
            List<PlantInventoryItem> availableItems = inventoryRepository.findAvailableItems(
                    plant.getPlantInfo().getId(),
                    po.getRentalPeriod().getStartDate(),
                    po.getRentalPeriod().getEndDate());

            availableItems.remove(plant);

            // 3. If no other plant available for the period - reject
            if (availableItems.size() == 0) {
                po.setStatus(POStatus.REJECTED);

                String template = "No available items to replace plant %d from %s to %s";
                String message = String.format(template, plant.getId(), startDate, endDate);
                throw new PlantNotAvailableException(message);
            } else {
                plant = availableItems.get(0);
                po.setPlant(plant);
                po.setPlantReplaced(true);
            }
        }

        // 4. Else - create reservation
        poRepository.save(po);
        PlantReservation reservation = plantReservationRepository.saveAndFlush(PlantReservation.of(null, po.getRentalPeriod(), null, plant, null));
        po.getReservations().add(reservation);

        poRepository.save(po);

        return purchaseOrderAssembler.toResource(po);
    }

    public PurchaseOrderDTO editPOPeriod(Long id, LocalDate startDate, LocalDate endDate) throws PurchaseOrderNotFoundException {
        BusinessPeriod period = BusinessPeriod.of(startDate, endDate);
        DataBinder binder = new DataBinder(period);
        binder.addValidators(new BusinessPeriodValidator());
        binder.validate();
        PurchaseOrder order = poRepository.findById(id).orElseThrow(() -> new PurchaseOrderNotFoundException(id));
        while (!order.getReservations().isEmpty()) {
            plantReservationRepository.delete(order.getReservations().remove(0));
        }
        order.setReservations(new ArrayList<>());
        boolean isItemAvailable = inventoryRepository.checkPlantIsAvailable(order.getPlant().getId(), startDate, endDate);
        if (!isItemAvailable) {
            order.setStatus(POStatus.REJECTED);
        } else {
            PlantReservation reservation = plantReservationRepository.saveAndFlush(PlantReservation.of(null, period, order, order.getPlant(), null));
            order.addReservation(reservation);
            order.setRentalPeriod(period);
        }
        order = poRepository.saveAndFlush(order);
        return purchaseOrderAssembler.toResource(order);
    }

    public PurchaseOrderDTO extendPO(Long id, LocalDate endDate) throws PurchaseOrderNotFoundException, ValidationException, PlantNotAvailableException {
        PurchaseOrder order = poRepository.findById(id).orElseThrow(() -> new PurchaseOrderNotFoundException(id));
        BusinessPeriod orderPeriod = BusinessPeriod.of(order.getRentalPeriod().getStartDate(), endDate);
        BusinessPeriod newReservationPeriod = BusinessPeriod.of(order.getRentalPeriod().getEndDate(), endDate);

        // not default BusinessPeriodValidator in order to make possible to change date, where startDate is before now
        DataBinder binder = new DataBinder(newReservationPeriod);
        binder.addValidators(new ExtendPOEndDateValidator());
        binder.validate();
        if (binder.getBindingResult().hasErrors())
            throw new ValidationException("period", binder.getBindingResult());

        boolean isItemAvailable = inventoryRepository.checkPlantIsAvailable(order.getPlant().getId(), newReservationPeriod.getStartDate(), endDate);
        if (isItemAvailable) {
            PlantReservation reservation = plantReservationRepository.saveAndFlush(PlantReservation.of(null, newReservationPeriod, order, order.getPlant(), null));
            order.addReservation(reservation);
        } else {
            List<PlantInventoryItem> availableItems = inventoryRepository.findAvailableItems(
                    order.getPlant().getPlantInfo().getId(),
                    newReservationPeriod.getStartDate(),
                    newReservationPeriod.getEndDate());
            if (availableItems.size() > 0) {
                PlantInventoryItem plant = availableItems.get(0);
                order.setPlant(plant);
                order.setPlantReplaced(true);
                PlantReservation reservation = plantReservationRepository.saveAndFlush(PlantReservation.of(null, newReservationPeriod, order, plant, null));
                order.addReservation(reservation);
            } else {
                String template = "Plant %d is not available from %s to %s and no other available items to replace it for requested period";
                String message = String.format(template, order.getPlant().getId(), newReservationPeriod.getStartDate(), newReservationPeriod.getEndDate());
                throw new PlantNotAvailableException(message);
            }
        }
        order.setRentalPeriod(orderPeriod);
        order = poRepository.saveAndFlush(order);
        return purchaseOrderAssembler.toResource(order);
    }

    public PurchaseOrderDTO acceptPO(Long id) throws Exception {
        PurchaseOrder po = getPO(id);
        if (po.getStatus() != POStatus.PENDING) {
            throw new PurchaseOrderStatusException(id, po.getStatus(), POStatus.ACCEPTED);
        }

        externalAPICommunicator.acceptPO(po.getCustomerOrderId());

        po.setStatus(POStatus.ACCEPTED);
        poRepository.save(po);

        return purchaseOrderAssembler.toResource(po);
    }

    public PurchaseOrderDTO rejectPO(Long id, String rejectReason) throws Exception {
        PurchaseOrder po = getPO(id);
        if (po.getStatus() != POStatus.PENDING) {
            throw new PurchaseOrderStatusException(id, po.getStatus(), POStatus.REJECTED);
        }

        while (!po.getReservations().isEmpty()) {
            plantReservationRepository.delete(po.getReservations().remove(0));
        }

        externalAPICommunicator.rejectPO(po.getCustomerOrderId(), rejectReason);

        po.setStatus(POStatus.REJECTED);
        poRepository.save(po);

        return purchaseOrderAssembler.toResource(po);
    }

    public PurchaseOrderDTO cancelPO(Long id) throws Exception {
        PurchaseOrder po = getPO(id);
        PlantInventoryItem item = po.getPlant();

        if (item.getStatus() != PlantStatus.AVAILABLE) {
            throw new PurchaseOrderCancellationException(id, item.getStatus());
        }

        po.setStatus(POStatus.CANCELLED);
        po = poRepository.saveAndFlush(po);

        return purchaseOrderAssembler.toResource(po);
    }

    public InvoiceDTO getInvoice(Long invoiceId) throws InvoiceNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        return invoiceAssembler.toResource(invoice);
    }

    public InvoiceDTO createInvoice(Long orderId) throws PurchaseOrderNotFoundException, InvoiceNotSentException, URISyntaxException {
        // would be better to have price calculated on rental period and all used plants
        PurchaseOrder order = poRepository.findById(orderId).orElseThrow(() -> new PurchaseOrderNotFoundException(orderId));
        Invoice invoice = Invoice.of(null, order.getPlant().getPlantInfo().getPrice(), order, InvoiceStatus.PENDING);
        invoice = invoiceRepository.saveAndFlush(invoice);
        externalAPICommunicator.submitInvoice(invoice);
        return invoiceAssembler.toResource(invoice);
    }

    public InvoiceDTO payInvoice(Long invoiceId) throws InvoiceNotFoundException, InvoiceStatusViolationException {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceStatusViolationException("Invoice is already paid");
        } else if (invoice.getStatus() == InvoiceStatus.REJECTED) {
            throw new InvoiceStatusViolationException("Invoice is rejected");
        }
        invoice.setStatus(InvoiceStatus.PAID);
        invoice = invoiceRepository.saveAndFlush(invoice);
        return invoiceAssembler.toResource(invoice);
    }

    public InvoiceDTO rejectInvoice(Long invoiceId) throws InvoiceNotFoundException, InvoiceStatusViolationException {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceStatusViolationException("Invoice is already paid");
        } else if (invoice.getStatus() == InvoiceStatus.REJECTED) {
            throw new InvoiceStatusViolationException("Invoice is already rejected");
        }
        invoice.setStatus(InvoiceStatus.REJECTED);
        invoice = invoiceRepository.saveAndFlush(invoice);
        return invoiceAssembler.toResource(invoice);
    }

    @Scheduled(fixedRate=6000)
    public void notifyAboutNotPaidOrders() throws Exception {
        System.out.println(String.format("Start job: notifying PENDING invoices! (%s)",  new Date()));
        List<Invoice> pendingInvoices = invoiceRepository.getAllByStatus(InvoiceStatus.PENDING);

        for (Invoice invoice: pendingInvoices) {
            if (invoice.getPurchaseOrder().getRentalPeriod().getEndDate().compareTo(LocalDate.now()) > 0) {
                Boolean result = externalAPICommunicator.submitNotificationABoutUnpaidInvoice(invoice);
            }
        }
    }

    private PurchaseOrder getPO(Long id) throws Exception {
        PurchaseOrder po = poRepository.findById(id).orElse(null);
        if (po == null) {
            throw new PurchaseOrderNotFoundException(id);
        }

        return po;
    }

    private Boolean isPlantItemAvailable(Long plantId, BusinessPeriod period) {
        LocalDate startDate = period.getStartDate();
        LocalDate endDate = period.getEndDate();
        List<PlantReservation> plantReservations =
                plantReservationRepository.findPlantItemReservations(plantId, startDate, endDate);

        return plantReservations.size() == 0;
    }

    private Boolean isPlantScheduledForMaintenance(Long plantId, LocalDate endDate) {
        List<MaintenanceTask> tasks = maintenanceTaskRepository.findMaintenanceUntilDate(plantId, endDate);
        return tasks.size() > 0;
    }
}