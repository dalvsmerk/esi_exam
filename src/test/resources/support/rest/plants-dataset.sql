insert into plant_inventory_entry (id, name, description, price)
    values (100, 'Mini excavator', '1.5 Tonne Mini excavator', 150);
insert into plant_inventory_entry (id, name, description, price)
    values (200, 'Mini excavator', '3 Tonne Mini excavator', 200);
insert into plant_inventory_entry (id, name, description, price)
    values (300, 'Midi excavator', '5 Tonne Midi excavator', 250);
insert into plant_inventory_entry (id, name, description, price)
    values (400, 'Midi excavator', '8 Tonne Midi excavator', 300);
insert into plant_inventory_entry (id, name, description, price)
    values (500, 'Maxi excavator', '15 Tonne Large excavator', 400);
insert into plant_inventory_entry (id, name, description, price)
    values (600, 'Maxi excavator', '20 Tonne Large excavator', 450);
insert into plant_inventory_entry (id, name, description, price)
    values (700, 'HS dumper', '1.5 Tonne Hi-Swivel Dumper', 150);
insert into plant_inventory_entry (id, name, description, price)
    values (800, 'FT dumper', '2 Tonne Front Tip Dumper', 180);
insert into plant_inventory_entry (id, name, description, price)
    values (900, 'FT dumper', '2 Tonne Front Tip Dumper', 200);
insert into plant_inventory_entry (id, name, description, price)
    values (1000, 'FT dumper', '2 Tonne Front Tip Dumper', 300);
insert into plant_inventory_entry (id, name, description, price)
    values (1100, 'FT dumper', '3 Tonne Front Tip Dumper', 400);
insert into plant_inventory_entry (id, name, description, price)
    values (1200, 'Loader', 'Hewden Backhoe Loader', 200);
insert into plant_inventory_entry (id, name, description, price)
    values (1300, 'D-Truck', '15 Tonne Articulating Dump Truck', 250);
insert into plant_inventory_entry (id, name, description, price)
    values (1400, 'D-Truck', '30 Tonne Articulating Dump Truck', 300);

insert into plant_inventory_item (id, plant_info_id, serial_number, equipment_condition, status)
    values (100, 100, 'A01', 'SERVICEABLE', 'AVAILABLE');
insert into plant_inventory_item (id, plant_info_id, serial_number, equipment_condition, status)
    values (200, 200, 'A02', 'SERVICEABLE', 'AVAILABLE');
insert into plant_inventory_item (id, plant_info_id, serial_number, equipment_condition, status)
    values (300, 300, 'A03', 'UNSERVICEABLEREPAIRABLE', 'AVAILABLE');
insert into plant_inventory_item (id, plant_info_id, serial_number, equipment_condition, status)
    values (400, 400, 'A03', 'UNSERVICEABLEREPAIRABLE', 'DISPATCHED');

insert into maintenance_plan (id, year_of_action, plant_id) values (100, 2017, 300);

insert into plant_reservation (id, plant_id, start_date, end_date, maintenance_id) values (100, 300, '2017-03-22', '2017-03-24', 100);

insert into maintenance_order (id, plant_id, start_date, end_date, status, description)
    values (100, 200, '2020-06-12', '2020-06-30', 'PENDING', 'I do not like the vehicle colour');
insert into maintenance_order (id, plant_id, start_date, end_date, status, description)
    values (200, 200, '2020-07-12', '2020-07-30', 'ACCEPTED', 'Needs repairing');
insert into maintenance_order (id, plant_id, start_date, end_date, status, description)
    values (300, 200, '2020-08-12', '2020-08-30', 'REJECTED', 'Needs repairing');
insert into maintenance_order (id, plant_id, start_date, end_date, status, description)
    values (400, 200, '2020-09-12', '2020-09-30', 'CANCELLED', 'Needs repairing');
insert into maintenance_order (id, plant_id, start_date, end_date, status, description)
    values (500, 200, '2020-10-12', '2020-10-30', 'COMPLETED', 'Needs repairing');
insert into maintenance_order (id, plant_id, start_date, end_date, status, description)
    values (600, 200, '2020-01-12', '2020-01-20', 'PENDING', 'Needs repairing');
insert into maintenance_order (id, plant_id, start_date, end_date, status, description)
    values (700, 200, '2020-02-12', '2020-02-20', 'ACCEPTED', 'Needs repairing');

insert into maintenance_plan (id, year_of_action, plant_id, order_id) values (200, 2020, 200, 700);

insert into plant_reservation (id, plant_id, start_date, end_date, maintenance_id) values (200, 200, '2020-02-12', '2020-02-20', 200);

insert into maintenance_task (id, description, price, start_date, end_date, type_of_work, reservation_id) values (100, 'task description', 150, '2020-02-12', '2020-02-20', 'CORRECTIVE', 200);

insert into maintenance_plan_tasks(maintenance_plan_id, tasks_id) values (200, 100);

update maintenance_order set maintenance_plan_id = 200 where id = 700;