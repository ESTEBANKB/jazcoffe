-- Agregar rol CLIENTE para usuarios clientes
insert into roles(nombre) 
select 'CLIENTE' 
where not exists (select 1 from roles where nombre = 'CLIENTE');

