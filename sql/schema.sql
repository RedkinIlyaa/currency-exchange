create table currencies
(
    id        serial
        primary key,
    code      varchar(3)   not null
        unique,
    full_name varchar(128) not null
        unique,
    sign      varchar(16)  not null
);

alter table currencies
    owner to postgres;

create table exchange_rates
(
    id                 serial
        primary key,
    base_currency_id   integer        not null
        references currencies,
    target_currency_id integer        not null
        references currencies,
    rate               numeric(12, 6) not null
        constraint exchange_rates_rate_check
            check (rate > (0)::numeric),
    unique (base_currency_id, target_currency_id),
    constraint currency_is_not_the_same
        check (base_currency_id <> target_currency_id)
);

alter table exchange_rates
    owner to postgres;

create unique index exchange_rates_unordered_pair_uq
    on exchange_rates (LEAST(base_currency_id, target_currency_id), GREATEST(base_currency_id, target_currency_id));

