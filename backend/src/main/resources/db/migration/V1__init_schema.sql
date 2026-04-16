create table pois (
    id varchar(64) primary key,
    name varchar(255) not null,
    city varchar(128) not null,
    address varchar(255) not null,
    latitude double precision not null,
    longitude double precision not null,
    source varchar(64) not null
);

create table poi_tags (
    poi_id varchar(64) not null references pois(id),
    tag varchar(64) not null,
    position integer not null,
    primary key (poi_id, position)
);

create table trips (
    id varchar(64) primary key,
    name varchar(255) not null,
    days integer not null,
    note text null
);

create table trip_pois (
    trip_id varchar(64) not null references trips(id),
    poi_id varchar(64) not null references pois(id),
    position integer not null,
    primary key (trip_id, position)
);

insert into pois (id, name, city, address, latitude, longitude, source) values
('poi-west-lake', '西湖断桥', '杭州', '浙江省杭州市西湖区北山街', 30.259, 120.148, 'seed'),
('poi-the-bund', '外滩观景步道', '上海', '上海市黄浦区中山东一路', 31.240, 121.490, 'seed'),
('poi-orange-island', '橘子洲头', '长沙', '湖南省长沙市岳麓区橘子洲景区', 28.189, 112.969, 'seed');

insert into poi_tags (poi_id, tag, position) values
('poi-west-lake', 'view', 0),
('poi-west-lake', 'lake', 1),
('poi-west-lake', 'classic', 2),
('poi-the-bund', 'citywalk', 0),
('poi-the-bund', 'night', 1),
('poi-orange-island', 'park', 0),
('poi-orange-island', 'landmark', 1);

insert into trips (id, name, days, note) values
('trip-1', '杭州周末慢游', 2, '以西湖周边 citywalk 为主');

insert into trip_pois (trip_id, poi_id, position) values
('trip-1', 'poi-west-lake', 0);
