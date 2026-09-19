INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'EUR'),
           (SELECT id FROM public.currencies WHERE code = 'RUB'),
           94.000000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'USD'),
           (SELECT id FROM public.currencies WHERE code = 'EUR'),
           0.850000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'USD'),
           (SELECT id FROM public.currencies WHERE code = 'CNY'),
           7.210000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'GBP'),
           (SELECT id FROM public.currencies WHERE code = 'USD'),
           1.340000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'USD'),
           (SELECT id FROM public.currencies WHERE code = 'JPY'),
           147.500000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'MXN'),
           (SELECT id FROM public.currencies WHERE code = 'SGD'),
           52.500000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'USD'),
           (SELECT id FROM public.currencies WHERE code = 'MXN'),
           650.000000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'CNY'),
           (SELECT id FROM public.currencies WHERE code = 'RUB'),
           12.100000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'CAD'),
           (SELECT id FROM public.currencies WHERE code = 'CHF'),
           62.000000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'CNY'),
           (SELECT id FROM public.currencies WHERE code = 'CHF'),
           12.000000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'AUD'),
           (SELECT id FROM public.currencies WHERE code = 'TRY'),
           0.500000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'USD'),
           (SELECT id FROM public.currencies WHERE code = 'TRY'),
           48.390000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'EUR'),
           (SELECT id FROM public.currencies WHERE code = 'TRY'),
           56.300000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'RUB'),
           (SELECT id FROM public.currencies WHERE code = 'TRY'),
           0.560000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'GBP'),
           (SELECT id FROM public.currencies WHERE code = 'TRY'),
           65.460000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'JPY'),
           (SELECT id FROM public.currencies WHERE code = 'TRY'),
           0.560000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'NZD'),
           (SELECT id FROM public.currencies WHERE code = 'THB'),
           20.000000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'CNY'),
           (SELECT id FROM public.currencies WHERE code = 'TRY'),
           7.220000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'CHF'),
           (SELECT id FROM public.currencies WHERE code = 'TRY'),
           59.890000
       );

INSERT INTO public.exchange_rates (
    base_currency_id,
    target_currency_id,
    rate
)
VALUES (
           (SELECT id FROM public.currencies WHERE code = 'USD'),
           (SELECT id FROM public.currencies WHERE code = 'RUB'),
           95.000000
       );