insert into plant_inventory_entry
    (id, name, description, price)
values
    (1, 'Mini excavator', '3 Tonne Mini excavator', 200),
    (2, 'FT dumper', '2 Tonne Front Tip Dumper', 300),
    (3, 'Large Truck', 'Large truck', 500),
    (4, 'Very Large Truck', 'Very large truck', 500),
    (5, 'Very Large Truck', 'Very large truck', 500);

insert into plant_inventory_item
    (id, plant_info_id, serial_number, equipment_condition, status)
values
    (1, 1, 'A01', 'SERVICEABLE', 'AVAILABLE'),
    (2, 2, 'A02', 'SERVICEABLE', 'AVAILABLE'),
    (3, 3, 'A03', 'SERVICEABLE', 'AVAILABLE'),
    (4, 3, 'A04', 'SERVICEABLE', 'AVAILABLE'),
    (5, 3, 'A05', 'SERVICEABLE', 'AVAILABLE'),
    (6, 4, 'A06', 'SERVICEABLE', 'AVAILABLE'), -- do not create items of this plant_info_id
    (7, 5, 'A06', 'SERVICEABLE', 'DISPATCHED'),
    (8, 5, 'A06', 'SERVICEABLE', 'DELIVERED'),
    (9, 5, 'A06', 'SERVICEABLE', 'REJECTED_BY_CUSTOMER'),
    (10, 5, 'A06', 'SERVICEABLE', 'AVAILABLE');

-- Purchase order
insert into purchase_order
    (id, plant_id, issue_date, status, start_date, end_date, plant_replaced, customer_order_id)
values
    (1, 2, '2020-05-01', 'ACCEPTED', '2020-06-01', '2020-06-05', false, 10),
    (100, 10, '2020-05-01', 'PENDING', '2020-06-01', '2020-06-05', false, 11),
    (101, 7, '2020-05-01', 'ACCEPTED', '2020-06-01', '2020-06-05', false, 13),
    (102, 8, '2020-05-01', 'ACCEPTED', '2020-06-01', '2020-06-05', false, 14),
    (103, 9, '2020-05-01', 'ACCEPTED', '2020-06-01', '2020-06-05', false, 15),
    (104, 8, '2020-05-01', 'ACCEPTED', '2020-02-01', '2020-02-05', false, 14);


insert into plant_reservation
    (id, start_date, end_date, rental_id, plant_id)
values
    (1, '2020-06-01', '2020-06-05', 1, 2);

-- Maintenance
insert into maintenance_plan
    (id, year_of_action, plant_id)
values
    (1, 2020, 3),
    (2, 2020, 6);

insert into plant_reservation
    (id, start_date, end_date, plant_id, maintenance_id)
values
    (2, '2020-07-01', '2020-07-05', 3, 1),
    (3, '2020-07-01', '2020-07-05', 6, 2);

insert into maintenance_task
    (id, description, type_of_work, plan_id, price, start_date, end_date, reservation_id)
values
    (1, 'Repair', 'CORRECTIVE', 1, 600, '2020-07-01', '2020-07-05', 2),
    (2, 'Repair', 'CORRECTIVE', 2, 800, '2020-07-01', '2020-07-05', 3);


-- Edit Purchase Order data
insert into purchase_order
    (id, plant_id, issue_date, status, start_date, end_date, plant_replaced, customer_order_id)
values
    (2, 1, '2020-05-01', 'ACCEPTED', '2020-06-01', '2020-06-05', false, 22),
    (3, 1, '2020-05-01', 'ACCEPTED', '2020-07-01', '2020-07-08', false, 33);

insert into plant_reservation
    (id, start_date, end_date, rental_id, plant_id)
values
    (4, '2020-06-01', '2020-06-05', 2, 1),
    (5, '2020-07-01', '2020-07-08', 3, 1);

insert into purchase_order_reservations
    (purchase_order_id, reservations_id)
values
    (2, 4),
    (3, 5);

insert into invoice
    (id, status, purchase_order_id, total)
values
    (1, 'PENDING', 1, 420),
    (2, 'PAID', 2, 420),
    (3, 'PAID', 104, 420);
