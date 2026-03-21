INSERT INTO tb_category(name)
VALUES ('Frozen');

SELECT *
FROM tb_category;

SELECT *
FROM tb_category
WHERE id = 1;

UPDATE tb_category
SET name='Tobacco'
WHERE id = 1;

DELETE
FROM tb_category
WHERE id = 11;
\

SELECT *
FROM tb_category ct
         INNER JOIN tb_product_category tpc ON ct.id = tpc.category_id;

INSERT INTO tb_product(name)
VALUES ('Sprite');

SELECT *
FROM tb_product;

SELECT pt.id, name, created_date, updated_date, is_active
FROM tb_product pt
         INNER JOIN tb_product_category tpc ON pt.id = tpc.product_id
WHERE tpc.category_id = 4;

SELECT *
FROM tb_product
WHERE name ILIKE '${name}';

SELECT *
FROM tb_category
WHERE name ILIKE 's';

INSERT INTO tb_category(name)
VALUES ('seafood');

SELECT *
FROM tb_category;

SELECT EXISTS(SELECT * FROM warehouse_master.public.tb_store_category WHERE category_id = 7);

SELECT EXISTS(SELECT * FROM tb_store_category WHERE store_id = 13 AND category_id = 21);

SELECT *
FROM tb_category
WHERE name = 'sea food';

INSERT INTO tb_category
VALUES (default, '', default, default);
SELECT *
FROM tb_store_category;



SELECT *
FROM tb_store_category;

SELECT *
FROM tb_store
WHERE distributor_account_id = 31;

SELECT *
FROM tb_distributor_account
WHERE id = 31;

select id
from tb_category
where name ILIKE 'tea';

INSERT INTO tb_category
VALUES (default, 'tea', default, default)
RETURNING 1;

select id
from tb_store
where distributor_account_id = 57;

SELECT *
FROM tb_store_category
WHERE store_id = 18
  AND category_id = 90;

SELECT *
FROM tb_category
WHERE name LIKE 'skinecare';
SELECT *
FROM tb_store_category
WHERE category_id = 64;

SELECT *
from tb_category
WHERE name ILIKE 'steak';

SELECT *
FROM tb_category;

SELECT TCG.id, name, created_date, updated_date
FROM tb_category TCG
         INNER JOIN tb_store_category TSC ON TCG.id = TSC
    .category_id
WHERE TSC.store_id = 18;

--
-- SELECT count(*) FROM tb_order
-- Where store_id=9 AND status_id=5 AND EXTRACT(MONTH FROM created_date) ;

SELECT count(*)
FROM tb_order
Where retailer_account_id = 34
  AND store_id = 25
  AND status_id = 5
  AND created_date BETWEEN '2023-05-01 14:25:44.000000' AND
    '2023-06-28 16:12:16'
        '.000000';


SELECT count(*)
FROM tb_order
Where retailer_account_id = 17
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 4
  AND EXTRACT(YEAR FROM
              created_date) = 2023;

SELECT count(*)
FROM tb_order
WHERE retailer_account_id = 17
  AND status_id = 4
  AND created_date BETWEEN '2023-05-03' AND '2024-05-26';

SELECT *
FROM tb_order
WHERE created_date BETWEEN '2020-05-26 10:26:18.873000' AND '2024-05-26 21:30:34.000000';


SELECT count(*)
FROM tb_order
WHERE retailer_account_id = 17
  AND status_id = 5
  AND created_date = '2020-05-26 10:26:18.873000';

SELECT *
FROM tb_order
WHERE date_trunc('day', your_date_column) BETWEEN '3-01-01' AND '2023-01-31';

SELECT count(*)
FROM tb_order
WHERE retailer_account_id = 17
  AND date_trunc('month', created_date) BETWEEN '2023-05-01' AND '2024-07-01';



SELECT count(*)
FROM tb_order
WHERE retailer_account_id = 17
  AND status_id = 6
  AND created_date BETWEEN '2023-02-01 00:00:00' AND '2023-07-31 23:59:59';



SELECT *
FROM tb_order_detail
         INNER JOIN tb_product TP ON TP.id = tb_order_detail.product_id;

SELECT count(*)
FROM tb_order_detail TOD
         INNER JOIN tb_order T ON T.id = TOD.order_id
Where retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 6
  AND EXTRACT(YEAR FROM
              created_date) = 2023;
-- GROUP BY retailer_account_id, created_date;

SELECT name
FROM tb_category
WHERE id = 100;

select name
from (tb_order_detail Inner Join tb_order t on t.id = tb_order_detail.order_id)
         INNER JOIN tb_store_product_detail
                    ON tb_order_detail.store_product_id = tb_store_product_detail.id
Where retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 6
  AND EXTRACT(YEAR FROM
              created_date) = 2023;

select *
from ((tb_order_detail Inner Join tb_order t on t.id = tb_order_detail.order_id) INNER JOIN
    tb_store_product_detail spd on spd.id = tb_order_detail.store_product_id)
         INNER JOIN tb_category TC ON TC.id = spd.category_id
Where retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 6
  AND EXTRACT(YEAR FROM
              created_date) = 2023;

select category_id
from tb_order_detail
         INNER JOIN tb_store_product_detail ON tb_order_detail.store_product_id = tb_store_product_detail
    .product_id;
-- Where retailer_account_id = 34 AND status_id=5 AND EXTRACT(MONTH FROM created_date) = 6 AND EXTRACT(YEAR FROM
--     created_date) =2023;

SELECT *
FROM tb_store_product_detail
WHERE store_id = 25;


SELECT DISTINCT TC.name
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
         INNER JOIN tb_store_product_detail TSPD ON TSPD.id = tb_order_detail.store_product_id
         INNER JOIN tb_category TC ON TC.id = TSPD.category_id
Where retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM TC.created_date) = 5
  AND EXTRACT(YEAR FROM
              TC.created_date) = 2022
ORDER BY TC.name DESC;



SELECT TS.name
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
         INNER JOIN tb_store TS ON TS.id = T.store_id
WHERE retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM T.created_date) = 6
  AND EXTRACT(YEAR FROM
              T.created_date)
    = 2023;

SELECT DISTINCT count(*)
FROM tb_order
WHERE retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 6
  AND EXTRACT
          (YEAR FROM created_date) = 2023;

SELECT sum(qty * unit_price)
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
WHERE retailer_account_id = 34
  AND store_id = 25
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 5
  AND EXTRACT
          (YEAR FROM created_date) = 2023;
SELECT sum(qty * unit_price)
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
WHERE T.retailer_account_id = 34
  AND T.status_id = 5
  AND EXTRACT(MONTH FROM T.created_date) = 6
  AND EXTRACT(YEAR FROM created_date) = 2023;

select *
from tb_order
where retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 6
  AND EXTRACT(YEAR FROM created_date) = 2023;

select (qty * unit_price) as total
from tb_order_detail
where order_id = 229;

select *
from tb_order_detail
where order_id = 217;

SELECT sum(qty) as totalQuantityOrder
FROM tb_order_detail TOD
         INNER JOIN tb_order T ON T.id = TOD.order_id
Where retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 6
  AND EXTRACT(YEAR FROM created_date) = 2023;

SELECT DISTINCT store_id
FROM tb_order
WHERE retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(MONTH FROM created_date) = 5
  AND EXTRACT(YEAR FROM created_date) = 2023;

SELECT count(*)
from tb_rating_detail
where retailer_account_id = 34
  AND EXTRACT(MONTH FROM created_date) = 6
  AND EXTRACT(YEAR FROM created_date) = 2023;


SELECT coalesce(sum(qty * unit_price), 0)
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
WHERE T.retailer_account_id = 34
  AND T.status_id = 5
  AND EXTRACT(Year FROM T.created_date) = 2022;

SELECT coalesce(sum(qty * unit_price), 0)
from tb_order_detail
         INNER JOIN tb_order t on t.id = tb_order_detail.order_id
where t.retailer_account_id = 34
  AND status_id = 5
  AND (EXTRACT(YEAR FROM T.created_date) = 2022 AND EXTRACT(MONTH FROM T.created_date) = 5
    OR (EXTRACT(YEAR FROM T.created_date) = 2023 AND EXTRACT(MONTH FROM T.created_date) = 6));



SELECT sum(qty)
from tb_order_detail tod
         INNER JOIN tb_order t on t.id = tod.order_id
Where retailer_account_id = 34
  AND status_id = 5
  AND (EXTRACT(YEAR FROM t.created_date) = 2022 AND EXTRACT(MONTH FROM t.created_date) = 5);


select count(*)
from tb_order
where status_id =
  AND store_id = 25;

2022-05-07 19:02:04.684000

SELECT sum(qty)
from tb_order_detail tod
         INNER JOIN tb_order t on t.id = tod.order_id
Where retailer_account_id = 34
  AND status_id = 5
  AND created_date
  AND 2022 - 05
  AND 2023 - 06 - 11



SELECT DISTINCT TC.name
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
         INNER JOIN tb_store_product_detail TSPD ON TSPD.id = tb_order_detail.store_product_id
         INNER JOIN tb_category TC ON TC.id = TSPD.category_id
Where retailer_account_id = 34
  AND status_id = 5
  AND EXTRACT(YEAR FROM TC.created_date) = 2023
  AND EXTRACT(MONTH FROM TC.created_date) = 6
ORDER BY TC.name DESC

SELECT store_id
FROM tb_order
WHERE retailer_account_id = 34
  AND status_id = 5
  AND (EXTRACT(YEAR FROM created_date) = 2022 AND EXTRACT(MONTH FROM created_date) = 5);
select *

from tb_order
where retailer_account_id = 34;

SELECT count(*)
from tb_rating_detail
where retailer_account_id = 34
  AND (EXTRACT(YEAR FROM created_date) = 2022 AND EXTRACT(MONTH FROM created_date) = 5);


SELECT sum(qty * unit_price)
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
WHERE T.retailer_account_id = 34
  AND T.status_id = 5
  AND (EXTRACT(YEAR FROM T.created_date) = 2023 AND EXTRACT(MONTH FROM T.created_date) = 6);

SELECT sum(qty * unit_price)
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
WHERE T.retailer_account_id = 34
  AND T.status_id = 5
  AND (EXTRACT(MONTH FROM T.created_date) = 5);

select *
from tb_order_detail
where retailer_account_id = 34
  AND T.status_id = 5
SELECT *
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
WHERE T.retailer_account_id = 34
  AND T.status_id = 5
  AND (EXTRACT(MONTH FROM T.created_date) = 5)
  AND (EXTRACT(YEAR FROM T.created_date) = 2022);

select created_date
from tb_category
where name = 'wine'

SELECT sum(tb_order_detail.qty) as totalItem ,TC.name as categoryName
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
         INNER JOIN tb_store_product_detail SPD ON SPD.id = tb_order_detail.store_product_id
         INNER JOIN tb_category TC ON TC.id = SPD.category_id
Where retailer_account_id = 34
  AND status_id = 5
  AND (EXTRACT(YEAR FROM T.created_date) = 2022 AND EXTRACT(MONTH FROM T.created_date) = 5)
group by TC.name
order by TC.name DESC ;


SELECT *
from tb_order
Where retailer_account_id = 34
  AND status_id = 5
  AND (EXTRACT(YEAR FROM created_date) = 2022 AND EXTRACT(MONTH FROM created_date) = 5);

select *
from tb_order_detail
where order_id in (217, 365, 366,229,
    438);

select *
from tb_store_product_detail
where id in (121,
    122,
123,
121,
205,
122
    );

select name
from tb_category
where id in (100,
88,
133
    );

SELECT coalesce(sum(qty))
from tb_order_detail tod
         INNER JOIN tb_order t on t.id = tod.order_id
Where retailer_account_id=34 AND status_id=25

  AND (EXTRACT(YEAR FROM T.created_date) = 2022 AND EXTRACT(MONTH FROM T.created_date) = 5);


SELECT sum(tb_order_detail.qty) as totalItem ,TC.name as categoryName
FROM tb_order_detail
         INNER JOIN tb_order T ON T.id = tb_order_detail.order_id
         INNER JOIN tb_store_product_detail SPD ON SPD.id = tb_order_detail.store_product_id
         INNER JOIN tb_category TC ON TC.id = SPD.category_id
Where retailer_account_id =34
  AND status_id = 5
  AND (EXTRACT(YEAR FROM T.created_date) = 2022 AND EXTRACT(MONTH FROM T.created_date) = 5)
group by TC.name
order by TC.name DESC


-- ===========================================Table==============================================
-- Script for table
CREATE TABLE IF NOT EXISTS tb_role (
                                       id   SERIAL PRIMARY KEY,
                                       name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS tb_distributor_account (
                                                      id           SERIAL PRIMARY KEY,
                                                      role_id      INTEGER      NOT NULL REFERENCES tb_role (id),
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     TEXT         NOT NULL,
    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_verified  BOOLEAN      DEFAULT FALSE,
    is_active    BOOLEAN      DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS tb_retailer_account (
                                                   id           SERIAL PRIMARY KEY,
                                                   role_id      INTEGER      NOT NULL REFERENCES tb_role (id),
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     TEXT         NOT NULL,
    created_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_verified  BOOLEAN      DEFAULT FALSE,
    is_active    BOOLEAN      DEFAULT TRUE
    );

INSERT INTO tb_role (id, name)
VALUES (1, 'DISTRIBUTOR')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO tb_role (id, name)
VALUES (2, 'RETAILER')
    ON CONFLICT (id) DO NOTHING;
