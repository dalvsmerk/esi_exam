package com.example.rentit.sales.application.service;
import com.example.rentit.sales.application.dto.InvoiceDTO;
import com.example.rentit.sales.domain.model.Invoice;
import com.example.rentit.sales.rest.SalesRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

@Service
public class InvoiceAssembler extends ResourceAssemblerSupport<Invoice, InvoiceDTO> {
    @Autowired
    PurchaseOrderAssembler purchaseOrderAssembler;

    public InvoiceAssembler() {
        super(SalesRestController.class, InvoiceDTO.class);
    }

    @Override
    public InvoiceDTO toResource(Invoice invoice) {
        if (invoice == null) return null;

        InvoiceDTO dto = createResourceWithId(invoice.getId(), invoice);
        dto.setPurchaseOrderDTO(purchaseOrderAssembler.toResource(invoice.getPurchaseOrder()));
        dto.setStatus(invoice.getStatus());
        dto.setTotal(invoice.getTotal());

        return dto;
    }
}
