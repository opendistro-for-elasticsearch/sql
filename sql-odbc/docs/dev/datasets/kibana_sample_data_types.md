# kibana_sample_data_types 

## Mapping

```json
PUT /kibana_sample_data_types
{  
    "mappings": {  
        "properties": {  
        "type_boolean" : { "type": "boolean"},  
        "type_byte" : { "type": "byte"},  
        "type_short" : { "type": "short"},  
        "type_integer" : { "type": "integer"},  
        "type_long" : { "type": "long"},  
        "type_half_float" : { "type": "half_float"},  
        "type_float" : { "type": "float"},  
        "type_double" : { "type": "double"},  
        "type_scaled_float" : {
            "type": "scaled_float",
            "scaling_factor": 100
        },  
        "type_keyword" : { "type": "keyword"},  
        "type_text" : { "type": "text"},  
        "type_date" : { "type": "date"},  
        "type_object" : { "type": "object"},  
        "type_nested" : { "type": "nested"}  
        }  
    }
}
```

## Data

```json
POST /kibana_sample_data_types/_doc
{
   "type_boolean": true,
   "type_byte" : -120,
   "type_short" : -2000,
   "type_integer" :-350000000,
   "type_long" : -8010000000,
   "type_half_float" : -2.115,
   "type_float" : -3.1512,
   "type_double" : -5335.2215,
   "type_scaled_float" : -100.1,
   "type_keyword" : "goodbye",
   "type_text" : "planet",
   "type_date" : "2016-02-21T12:23:52.803Z",
   "type_object" : { "foo" : "bar" },
   "type_nested" : {"foo":"bar"}
}
```

```json
POST /kibana_sample_data_types/_doc  
{
   "type_boolean": false,
   "type_byte" : 100,
   "type_short" : 1000,
   "type_integer" : 250000000,
   "type_long" : 8000000000,
   "type_half_float" : 1.115,
   "type_float" : 2.1512,
   "type_double" : 25235.2215,
   "type_scaled_float" : 100,
   "type_keyword" : "hello",
   "type_text" : "world",
   "type_date" : "2018-07-22T12:23:52.803Z",
   "type_object" : { "foo" : "bar" },
   "type_nested" : {"foo":"bar"}
}
```