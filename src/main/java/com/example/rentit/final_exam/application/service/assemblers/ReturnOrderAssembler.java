package com.example.rentit.final_exam.application.service.assemblers;

import com.example.rentit.final_exam.application.dto.ReturnOrderDTO;
import com.example.rentit.final_exam.domain.model.ReturnOrder;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import com.example.rentit.sales.application.service.PurchaseOrderAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReturnOrderAssembler extends ResourceAssemblerSupport<ReturnOrder, ReturnOrderDTO> {
    @Autowired
    PurchaseOrderAssembler poAssembler;

    public ReturnOrderAssembler() {
        super(ReturnOrder.class, ReturnOrderDTO.class);
    }

    @Override
    public ReturnOrderDTO toResource(ReturnOrder order) {
        ReturnOrderDTO dto = new ReturnOrderDTO();
        List<PurchaseOrderDTO> poDTOs = poAssembler.toResources(order.getOrders());

        dto.set_id(order.getId());
        dto.setFee(order.getFee());
        dto.setReturnDate(order.getReturnDate());
        dto.setStatus(order.getStatus());
        dto.setOrders(poDTOs);

        // TODO: Add hyperlinks to resource operations

        return dto;
    }
}
