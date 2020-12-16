/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */
// @ts-ignore
import { SortableProperty } from "@elastic/eui/lib/services";

export const mockQueryResults = [
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Teddy",
            "customer_full_name": "Teddy Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "Teddy@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "teddy",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 1,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 2,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 3,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 4,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 5,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 6,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 7,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 8,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 9,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 10,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 11,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        },
        {
          rowId: 12,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  },
  {
    "fulfilled": true,
    "data": {
      "fields": [
        "category",
        "currency",
        "customer_first_name",
        "customer_full_name",
        "customer_gender",
        "customer_id",
        "customer_last_name",
        "customer_phone",
        "day_of_week",
        "day_of_week_i",
        "email",
        "manufacturer",
        "order_date",
        "order_id",
        "products",
        "sku",
        "taxful_total_price",
        "taxless_total_price",
        "total_quantity",
        "total_unique_products",
        "type",
        "user",
        "geoip"
      ],
      "records": [
        {
          rowId: 0,
          data:
          {
            "category": [
              "Men's Clothing"
            ],
            "currency": "EUR",
            "customer_first_name": "Eddie",
            "customer_full_name": "Eddie Underwood",
            "customer_gender": "MALE",
            "customer_id": 38,
            "customer_last_name": "Underwood",
            "customer_phone": "",
            "day_of_week": "Monday",
            "day_of_week_i": 0,
            "email": "eddie@underwood-family.zzz",
            "manufacturer": [
              "Elitelligence",
              "Oceanavigations"
            ],
            "order_date": "2019-10-07T09:28:48+00:00",
            "order_id": 584677,
            "products": [
              {
                "base_price": 11.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Elitelligence",
                "tax_amount": 0,
                "product_id": 6283,
                "category": "Men's Clothing",
                "sku": "ZO0549605496",
                "taxless_price": 11.99,
                "unit_discount_amount": 0,
                "min_price": 6.35,
                "_id": "sold_product_584677_6283",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Basic T-shirt - dark blue/white",
                "price": 11.99,
                "taxful_price": 11.99,
                "base_unit_price": 11.99
              },
              {
                "base_price": 24.99,
                "discount_percentage": 0,
                "quantity": 1,
                "manufacturer": "Oceanavigations",
                "tax_amount": 0,
                "product_id": 19400,
                "category": "Men's Clothing",
                "sku": "ZO0299602996",
                "taxless_price": 24.99,
                "unit_discount_amount": 0,
                "min_price": 11.75,
                "_id": "sold_product_584677_19400",
                "discount_amount": 0,
                "created_on": "2016-12-26T09:28:48+00:00",
                "product_name": "Sweatshirt - grey multicolor",
                "price": 24.99,
                "taxful_price": 24.99,
                "base_unit_price": 24.99
              }
            ],
            "sku": [
              "ZO0549605496",
              "ZO0299602996"
            ],
            "taxful_total_price": 36.98,
            "taxless_total_price": 36.98,
            "total_quantity": 2,
            "total_unique_products": 2,
            "type": "order",
            "user": "eddie",
            "geoip": {
              "country_iso_code": "EG",
              "location": {
                "lon": 31.3,
                "lat": 30.1
              },
              "region_name": "Cairo Governorate",
              "continent_name": "Africa",
              "city_name": "Cairo"
            }
          }
        }
      ],
      "message": "Successfull"
    }
  }
];

export const mockSortableColumns = getSortableColumns();

function getSortableColumns() {
  const sortableColumns: Array<SortableProperty<string>> = [];
  mockQueryResults[0].data.fields.map((field: string) => {
    sortableColumns.push({
      name: field,
      getValue: (item: any) => item[field],
      isAscending: true
    });
  });
  return sortableColumns;
}


export const mockQueries = ["select * from index_1",
  "select * from index_2",
  "select * from index_3",
  "select * from index_4",
  "select * from index_5",
  "select * from index_6",
  "select * from index_7",
  "select * from index_8",
  "select * from index_9",
  "select * from index_10"];
export const mockErrorMessage = [{ text: 'Error', className: 'error-message' }];
export const mockSuccessfulMessage = [{ text: 'Successfull', className: 'successful-message' }];

export const mockExpandedMap = new Map<number, { nodes: {}; expandedRow: {}; selectedNodes: {}; }>();
mockExpandedMap[0] = {
  expandedRow: {},
  nodes: {},
  selectedNodes: { "0_manufacturer": ["Elitelligence", "Oceanavigations"] }
};

export const mockQueryResultJSONResponse = {
  "data": {
    "ok": true,
    "resp": "{\"took\":6,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,\"skipped\":0,\"failed\":0},\"hits\":{\"total\":4675,\"max_score\":1,\"hits\":[{\"_index\":\"kibana_sample_data_ecommerce\",\"_type\":\"_doc\",\"_id\":\"zpOZZG0BTX1z5xbJBaBA\",\"_score\":1,\"_source\":{\"category\":[\"Men's Clothing\"],\"currency\":\"EUR\",\"customer_first_name\":\"Eddie\",\"customer_full_name\":\"Eddie Underwood\",\"customer_gender\":\"MALE\",\"customer_id\":38,\"customer_last_name\":\"Underwood\",\"customer_phone\":\"\",\"day_of_week\":\"Monday\",\"day_of_week_i\":0,\"email\":\"eddie@underwood-family.zzz\",\"manufacturer\":[\"Elitelligence\",\"Oceanavigations\"],\"order_date\":\"2019-10-07T09:28:48+00:00\",\"order_id\":584677,\"products\":[{\"base_price\":11.99,\"discount_percentage\":0,\"quantity\":1,\"manufacturer\":\"Elitelligence\",\"tax_amount\":0,\"product_id\":6283,\"category\":\"Men's Clothing\",\"sku\":\"ZO0549605496\",\"taxless_price\":11.99,\"unit_discount_amount\":0,\"min_price\":6.35,\"_id\":\"sold_product_584677_6283\",\"discount_amount\":0,\"created_on\":\"2016-12-26T09:28:48+00:00\",\"product_name\":\"Basic T-shirt - dark blue/white\",\"price\":11.99,\"taxful_price\":11.99,\"base_unit_price\":11.99},{\"base_price\":24.99,\"discount_percentage\":0,\"quantity\":1,\"manufacturer\":\"Oceanavigations\",\"tax_amount\":0,\"product_id\":19400,\"category\":\"Men's Clothing\",\"sku\":\"ZO0299602996\",\"taxless_price\":24.99,\"unit_discount_amount\":0,\"min_price\":11.75,\"_id\":\"sold_product_584677_19400\",\"discount_amount\":0,\"created_on\":\"2016-12-26T09:28:48+00:00\",\"product_name\":\"Sweatshirt - grey multicolor\",\"price\":24.99,\"taxful_price\":24.99,\"base_unit_price\":24.99}],\"sku\":[\"ZO0549605496\",\"ZO0299602996\"],\"taxful_total_price\":36.98,\"taxless_total_price\":36.98,\"total_quantity\":2,\"total_unique_products\":2,\"type\":\"order\",\"user\":\"eddie\",\"geoip\":{\"country_iso_code\":\"EG\",\"location\":{\"lon\":31.3,\"lat\":30.1},\"region_name\":\"Cairo Governorate\",\"continent_name\":\"Africa\",\"city_name\":\"Cairo\"}}}]}}"
  }
};

export const mockQueryResultCSVResponse =
{
  "data": {
    "ok": true,
    "resp": "\"geoip,customer_first_name,customer_phone,type,manufacturer,products,customer_full_name,order_date,customer_last_name,day_of_week_i,total_quantity,currency,taxless_total_price,total_unique_products,category,customer_id,sku,order_id,user,customer_gender,email,day_of_week,taxful_total_price\\n{continent_name=Africa, city_name=Cairo, country_iso_code=EG, location={lon=31.3, lat=30.1}, region_name=Cairo Governorate},Eddie,,order,[Elitelligence, Oceanavigations],[{tax_amount=0, taxful_price=11.99, quantity=1, taxless_price=11.99, discount_amount=0, base_unit_price=11.99, discount_percentage=0, product_name=Basic T-shirt - dark blue/white, manufacturer=Elitelligence, min_price=6.35, created_on=2016-12-26T09:28:48+00:00, unit_discount_amount=0, price=11.99, product_id=6283, base_price=11.99, _id=sold_product_584677_6283, category=Men's Clothing, sku=ZO0549605496}, {tax_amount=0, taxful_price=24.99, quantity=1, taxless_price=24.99, discount_amount=0, base_unit_price=24.99, discount_percentage=0, product_name=Sweatshirt - grey multicolor, manufacturer=Oceanavigations, min_price=11.75, created_on=2016-12-26T09:28:48+00:00, unit_discount_amount=0, price=24.99, product_id=19400, base_price=24.99, _id=sold_product_584677_19400, category=Men's Clothing, sku=ZO0299602996}],Eddie Underwood,2019-10-07T09:28:48+00:00,Underwood,0,2,EUR,36.98,2,[Men's Clothing],38,[ZO0549605496, ZO0299602996],584677,eddie,MALE,eddie@underwood-family.zzz,Monday,36.98\""
  }
};

export const mockQueryResultJDBCResponse =
{
  "data": {
    "ok": true,
    "resp": "{\"schema\":[{\"name\":\"type\",\"type\":\"keyword\"},{\"name\":\"day_of_week_i\",\"type\":\"integer\"},{\"name\":\"total_quantity\",\"type\":\"integer\"},{\"name\":\"taxless_total_price\",\"type\":\"half_float\"},{\"name\":\"total_unique_products\",\"type\":\"integer\"},{\"name\":\"sku\",\"type\":\"keyword\"},{\"name\":\"customer_first_name\",\"type\":\"text\"},{\"name\":\"customer_phone\",\"type\":\"keyword\"},{\"name\":\"customer_full_name\",\"type\":\"text\"},{\"name\":\"order_id\",\"type\":\"keyword\"},{\"name\":\"manufacturer\",\"type\":\"text\"},{\"name\":\"customer_last_name\",\"type\":\"text\"},{\"name\":\"currency\",\"type\":\"keyword\"},{\"name\":\"email\",\"type\":\"keyword\"},{\"name\":\"day_of_week\",\"type\":\"keyword\"},{\"name\":\"customer_birth_date\",\"type\":\"date\"},{\"name\":\"order_date\",\"type\":\"date\"},{\"name\":\"category\",\"type\":\"text\"},{\"name\":\"customer_id\",\"type\":\"keyword\"},{\"name\":\"user\",\"type\":\"keyword\"},{\"name\":\"customer_gender\",\"type\":\"keyword\"},{\"name\":\"taxful_total_price\",\"type\":\"half_float\"}],\"total\":4675,\"datarows\":[[\"order\",0,2,36.98,2,[\"ZO0549605496\",\"ZO0299602996\"],\"Eddie\",\"\",\"Eddie Underwood\",584677,[\"Elitelligence\",\"Oceanavigations\"],\"Underwood\",\"EUR\",\"eddie@underwood-family.zzz\",\"Monday\",null,\"2019-10-07T09:28:48+00:00\",[\"Men's Clothing\"],38,\"eddie\",\"MALE\",36.98],[\"order\",6,2,53.98,2,[\"ZO0489604896\",\"ZO0185501855\"],\"Mary\",\"\",\"Mary Bailey\",584021,[\"Champion Arts\",\"Pyramidustries\"],\"Bailey\",\"EUR\",\"mary@bailey-family.zzz\",\"Sunday\",null,\"2019-10-06T21:59:02+00:00\",[\"Women's Clothing\"],20,\"mary\",\"FEMALE\",53.98]],\"size\":200,\"status\":200}"
  }
};

export const mockQueryTranslationResponse =
{
  "data": {
    "ok": true,
    "resp": "{\"from\":0,\"size\":200}"
  }
};

export const mockNotOkQueryResultResponse =
{
  "data": {
    "ok": false,
    "resp": ""
  }
};
