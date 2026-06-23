-- Usuarios y roles
create table if not exists roles (
    id bigserial primary key,
    nombre varchar(50) not null unique
);

create table if not exists usuarios (
    id bigserial primary key,
    nombre varchar(100) not null,
    email varchar(120) not null unique,
    hash varchar(255) not null,
    rol_id bigint not null references roles(id),
    activo boolean not null default true
);

-- Catalogos
create table if not exists categorias (
    id bigserial primary key,
    nombre varchar(80) not null unique
);

create table if not exists productos (
    id bigserial primary key,
    nombre varchar(120) not null,
    categoria_id bigint references categorias(id),
    precio numeric(12,2) not null,
    costo numeric(12,2) not null,
    stock_actual int not null default 0,
    stock_min int not null default 0
);

-- Inventario
create table if not exists movimientos_inventario (
    id bigserial primary key,
    producto_id bigint not null references productos(id),
    tipo varchar(20) not null,
    cantidad int not null,
    motivo varchar(200),
    usuario_id bigint references usuarios(id),
    fecha timestamp not null default current_timestamp
);

-- Ventas y POS
create table if not exists ventas (
    id bigserial primary key,
    fecha timestamp not null default current_timestamp,
    usuario_id bigint references usuarios(id),
    total numeric(12,2) not null,
    metodo_pago varchar(30) not null,
    estado varchar(20) not null
);

create table if not exists ventas_detalle (
    id bigserial primary key,
    venta_id bigint not null references ventas(id) on delete cascade,
    producto_id bigint not null references productos(id),
    cantidad int not null,
    precio_unitario numeric(12,2) not null
);

create table if not exists pagos (
    id bigserial primary key,
    venta_id bigint not null references ventas(id) on delete cascade,
    tipo varchar(20) not null,
    monto numeric(12,2) not null,
    referencia varchar(120),
    estado varchar(20) not null
);

create table if not exists cierres_caja (
    id bigserial primary key,
    usuario_id bigint references usuarios(id),
    fecha_inicio timestamp not null,
    fecha_fin timestamp,
    total_ventas numeric(12,2) not null default 0,
    total_efectivo numeric(12,2) not null default 0,
    total_tarjeta numeric(12,2) not null default 0,
    diferencias numeric(12,2) not null default 0
);

-- Datos iniciales
insert into roles(nombre) values ('ADMIN'), ('CAJERO');
insert into usuarios(nombre, email, hash, rol_id, activo)
values ('Administrador', 'admin@local', '$2a$10$abcdefghijklmnopqrstuv', 1, true);


