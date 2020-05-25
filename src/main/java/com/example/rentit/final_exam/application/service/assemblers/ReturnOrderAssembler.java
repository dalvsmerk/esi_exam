package com.example.rentit.final_exam.application.service.assemblers;

import com.example.rentit.common.rest.ExtendedLink;
import com.example.rentit.final_exam.application.dto.ReturnOrderDTO;
import com.example.rentit.final_exam.domain.model.ReturnOrder;
import com.example.rentit.final_exam.rest.ReturnOrderRestController;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import com.example.rentit.sales.application.service.PurchaseOrderAssembler;
import com.example.rentit.support.rest.MaintenanceRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpMethod.PATCH;

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

        try {
            switch (order.getStatus()) {
                case PENDING:
                    dto.add(new ExtendedLink(
                            linkTo(methodOn(ReturnOrderRestController.class).acceptReturnOrder(dto.get_id())).toString(),
                            "accept",
                            PATCH));
//                    dto.add(new ExtendedLink(
//                            linkTo(methodOn(ReturnOrderRestController.class).rejectReturnOrder(dto.get_id())).toString(),
//                            "reject",
//                            PATCH));
            }
        } catch (Exception e) {
            System.err.println("Error adding a hyperlink");
        }

        return dto;
    }
}
