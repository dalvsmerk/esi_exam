insert into plant_inventory_entry (id, name, description, price)
    values (100, 'Mini excavator', '1.5 Tonne Mini excavator', 150);

insert into plant_inventory_item (id, plant_info_id, serial_number, equipment_condition, status)
    values (100, 100, 'A01', 'SERVICEABLE', 'AVAILABLE');

insert into plant_inventory_item (id, plant_info_id, serial_number, equipment_condition, status)
    values (200, 100, 'A02', 'UNSERVICEABLECONDEMNED', 'AVAILABLE');

insert into plant_inventory_item (id, plant_info_id, serial_number, equipment_condition, status)
    values (300, 100, 'A03', 'UNSERVICEABLEREPAIRABLE', 'AVAILABLE');

insert into plant_inventory_item (id, plant_info_id, serial_number, equipment_condition, status)
    values (400, 100, 'A04', 'UNSERVICEABLEREPAIRABLE', 'AVAILABLE');

insert into maintenance_plan (id, year_of_action, plant_id, order_id) values (100, 2020, 300, null);
insert into plant_reservation (id, plant_id, start_date, end_date, maintenance_id) values (100, 300, '2020-06-01', '2020-06-05', 100);
insert into maintenance_task (id, description, price, start_date, end_date, type_of_work, reservation_id) values (100, 'task description', 150, '2020-06-01', '2020-06-05', 'OPERATIVE', 100);
insert into maintenance_plan_tasks(maintenance_plan_id, tasks_id) values (100, 100);

insert into maintenance_plan (id, year_of_action, plant_id, order_id) values (200, 2020, 400, null);
insert into plant_reservation (id, plant_id, start_date, end_date, maintenance_id) values (200, 400, '2020-07-07','2020-07-09', 200);
insert into maintenance_task (id, description, price, start_date, end_date, type_of_work, reservation_id) values (200, 'task description', 150, '2020-07-07', '2020-07-09', 'OPERATIVE', 200);
insert into maintenance_plan_tasks(maintenance_plan_id, tasks_id) values (200, 200);

insert into purchase_order (id, plant_id, issue_date, status, start_date, end_date, plant_replaced, customer_order_id)
    values (1, 100, '2020-05-05', 'ACCEPTED', '2020-08-12', '2020-06-05', false, 1),