package com.example.rentit.common.application.service;

import com.example.rentit.common.application.exception.ExternalApiCallException;
import com.example.rentit.common.application.exception.InvoiceNotSentException;
import com.example.rentit.sales.application.dto.ConstructorInvoiceDTO;
import com.example.rentit.sales.domain.model.Invoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class ExternalAPICommunicator {
    @Value("${rentit.host}")
    String rentItHost;
    @Value("${buildit.host}")
    String buildItHost;

    public Boolean submitInvoice(Invoice invoice) throws InvoiceNotSentException, URISyntaxException {
        String supplierInvoiceLink = rentItHost + "/api/sales/invoices/" + invoice.getId();
        ConstructorInvoiceDTO constructorInvoiceDTO = ConstructorInvoiceDTO.of(invoice.getTotal(), supplierInvoiceLink, invoice.getPurchaseOrder().getId());
        final String baseUrl = buildItHost + "/api/hire/invoices";
        URI uri = new URI(baseUrl);
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(uri, constructorInvoiceDTO, String.class);
        }
        catch (Exception e) {
            throw new InvoiceNotSentException(invoice);
        }
        return true;
    }

    public Boolean acceptPO(Long customerOrderId) throws ExternalApiCallException {
        String path = buildItHost +"/api/hire/purchase-orders/" + customerOrderId.toString() + "/accept";
        try {
            URI uri = new URI(path);
            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
            restTemplate.postForObject(uri, null, Object.class);

            return true;
        } catch (Exception e) {
            throw new ExternalApiCallException(path);
        }
    }

    public Boolean rejectPO(Long customerOrderId, String rejectReason) throws ExternalApiCallException {
        String path = buildItHost + "/api/hire/purchase-orders/" + customerOrderId.toString() + "/reject?rejectReason=" + rejectReason;
        try {
            URI uri = new URI(path);
            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
            restTemplate.postForObject(uri, null, Object.class);

            return true;
        } catch (Exception e) {
            throw new ExternalApiCallException(path);
        }
    }

    public Boolean submitNotificationABoutUnpaidInvoice(Invoice invoice) throws ExternalApiCallException {
        String path = buildItHost + "/api/hire/purchase-orders/" + invoice.getPurchaseOrder().getId() + "/pay-notification";

        String supplierInvoiceLink = rentItHost + "/api/sales/invoices/" + invoice.getPurchaseOrder().getId();
        ConstructorInvoiceDTO constructorInvoiceDTO = ConstructorInvoiceDTO.of(invoice.getTotal(), supplierInvoiceLink, invoice.getPurchaseOrder().getId());
        try {
            URI uri = new URI(path);
            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
            restTemplate.postForObject(uri, constructorInvoiceDTO, Object.class);

        } catch (Exception e) {
            throw new ExternalApiCallException(path);
        }
        return true;
    }
}
