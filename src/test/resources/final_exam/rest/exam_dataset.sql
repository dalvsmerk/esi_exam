insert into plant_inventory_entry
    (id, name, description, price)
values
    (1, 'Mini excavator', '3 Tonne Mini excavator', 200),
    (2, 'FT dumper', '2 Tonne Front Tip Dumper', 300),
    (3, 'Large Truck', 'Large truck', 500),
    (4, 'Very Large Truck', 'Very large truck', 500);

insert into plant_inventory_item
    (id, plant_info_id, serial_number, equipment_condition, status)
values
    (1, 1, 'A01', 'SERVICEABLE', 'AVAILABLE'),
    (2, 2, 'A02', 'SERVICEABLE', 'AVAILABLE'),
    (3, 3, 'A03', 'SERVICEABLE', 'AVAILABLE'),
    (4, 4, 'A04', 'SERVICEABLE', 'AVAILABLE'),
    (5, 4, 'A04', 'SERVICEABLE', 'AVAILABLE');

insert into purchase_order
    (id, plant_id, issue_date, status, start_date, end_date, plant_replaced, customer_order_id)
values
    (1, 1, '2020-05-01', 'ACCEPTED', '2020-05-01', '2020-07-08', false, 1),
    (2, 2, '2020-05-10', 'ACCEPTED', '2020-05-01', '2020-07-08', false, 2),
    (3, 3, '2020-06-01', 'ACCEPTED', '2020-05-01', '2020-07-08', false, 3),
    (4, 4, '2020-06-01', 'ACCEPTED', '2020-01-01', '2020-07-08', false, 4),
    (5, 5, '2020-06-01', 'ACCEPTED', '2020-07-01', '2020-07-08', false, 5);

-- insert into plant_reservation
--     (id, start_date, end_date, rental_id, plant_id)
-- values
--     (1, '2020-07-01', '2020-07-08', 1, 1),
--     (2, '2020-07-01', '2020-07-08', 2, 2),
--     (3, '2020-07-01', '2020-07-08', 3, 3);
--
-- insert into purchase_order_reservations
--     (purchase_order_id, reservations_id)
-- values
--     (1, 1),
--     (2, 2),
--     (3, 3);