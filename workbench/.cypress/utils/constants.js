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

export const delay = 1000;

export const verifyDownloadData = [
  {
    title: 'Download and verify JSON',
    url: 'api/sql_console/sqljson',
    file: 'JSONFile'
  },
  {
    title: 'Download and verify JDBC',
    url: 'api/sql_console/sqlquery',
    file: 'JDBCFile'
  },
  {
    title: 'Download and verify CSV',
    url: 'api/sql_console/sqlcsv',
    file: 'CSVFile'
  },
  {
    title: 'Download and verify Text',
    url: 'api/sql_console/sqltext',
    file: 'TextFile'
  },
];

export const testQueries = [
  {
    title: 'Test GROUP BY',
    query: 'select count(*) from accounts group by gender;',
    cell_idx: 3,
    expected_string: '507'
  },
  {
    title: 'Test GROUP BY with aliases and scalar function',
    query: 'SELECT ABS(age) AS a FROM accounts GROUP BY ABS(age);',
    cell_idx: 17,
    expected_string: '28.0'
  },
  {
    title: 'Test GROUP BY and HAVING',
    query: 'SELECT age, MAX(balance) FROM accounts GROUP BY age HAVING MIN(balance) > 3000;',
    cell_idx: 5,
    expected_string: '47257'
  },
  {
    title: 'Test ORDER BY',
    query: 'SELECT account_number FROM accounts ORDER BY account_number DESC;',
    cell_idx: 5,
    expected_string: '998'
  },
  {
    title: 'Test JOIN',
    query: 'select a.account_number, a.firstname, a.lastname, e.id, e.name from accounts a join employee_nested e order by a.account_number;',
    cell_idx: 45,
    expected_string: 'Duke'
  },
];

export const files = {
  JSONFile:
    `"hits":[{"_index":"accounts","_type":"_doc","_id":"842","_score":0,"_source":{"account_number":842,"balance":49587,"firstname":"Meagan","lastname":"Buckner","age":23,"gender":"F","address":"833 Bushwick Court","employer":"Biospan","email":"meaganbuckner@biospan.com","city":"Craig","state":"TX"}},{"_index":"accounts","_type":"_doc","_id":"854","_score":0,"_source":{"account_number":854,"balance":49795,"firstname":"Jimenez","lastname":"Barry","age":25,"gender":"F","address":"603 Cooper Street","employer":"Verton","email":"jimenezbarry@verton.com","city":"Moscow","state":"AL"}},{"_index":"accounts","_type":"_doc","_id":"97","_score":0,"_source":{"account_number":97,"balance":49671,"firstname":"Karen","lastname":"Trujillo","age":40,"gender":"F","address":"512 Cumberland Walk","employer":"Tsunamia","email":"karentrujillo@tsunamia.com","city":"Fredericktown","state":"MO"}},{"_index":"accounts","_type":"_doc","_id":"168","_score":0,"_source":{"account_number":168,"balance":49568,"firstname":"Carissa","lastname":"Simon","age":20,"gender":"M","address":"975 Flatbush Avenue","employer":"Zillacom","email":"carissasimon@zillacom.com","city":"Neibert","state":"IL"}},{"_index":"accounts","_type":"_doc","_id":"240","_score":0,"_source":{"account_number":240,"balance":49741,"firstname":"Oconnor","lastname":"Clay","age":35,"gender":"F","address":"659 Highland Boulevard","employer":"Franscene","email":"oconnorclay@franscene.com","city":"Kilbourne","state":"NH"}},{"_index":"accounts","_type":"_doc","_id":"803","_score":0,"_source":{"account_number":803,"balance":49567,"firstname":"Marissa","lastname":"Spears","age":25,"gender":"M","address":"963 Highland Avenue","employer":"Centregy","email":"marissaspears@centregy.com","city":"Bloomington","state":"MS"}},{"_index":"accounts","_type":"_doc","_id":"248","_score":0,"_source":{"account_number":248,"balance":49989,"firstname":"West","lastname":"England","age":36,"gender":"M","address":"717 Hendrickson Place","employer":"Obliq","email":"westengland@obliq.com","city":"Maury","state":"WA"}}]`,
  JDBCFile:
    `{"schema":[{"name":"account_number","type":"long"},{"name":"firstname","type":"text"},{"name":"gender","type":"text"},{"name":"city","type":"text"},{"name":"balance","type":"long"},{"name":"employer","type":"text"},{"name":"state","type":"text"},{"name":"email","type":"text"},{"name":"address","type":"text"},{"name":"lastname","type":"text"},{"name":"age","type":"long"}],"total":7,"datarows":[[842,"Meagan","F","Craig",49587,"Biospan","TX","meaganbuckner@biospan.com","833 Bushwick Court","Buckner",23],[854,"Jimenez","F","Moscow",49795,"Verton","AL","jimenezbarry@verton.com","603 Cooper Street","Barry",25],[97,"Karen","F","Fredericktown",49671,"Tsunamia","MO","karentrujillo@tsunamia.com","512 Cumberland Walk","Trujillo",40],[168,"Carissa","M","Neibert",49568,"Zillacom","IL","carissasimon@zillacom.com","975 Flatbush Avenue","Simon",20],[240,"Oconnor","F","Kilbourne",49741,"Franscene","NH","oconnorclay@franscene.com","659 Highland Boulevard","Clay",35],[803,"Marissa","M","Bloomington",49567,"Centregy","MS","marissaspears@centregy.com","963 Highland Avenue","Spears",25],[248,"West","M","Maury",49989,"Obliq","WA","westengland@obliq.com","717 Hendrickson Place","England",36]],"size":7,"status":200}`,
  CSVFile:
    `account_number,firstname,address,balance,gender,city,employer,state,age,email,lastname
842,Meagan,833 Bushwick Court,49587,F,Craig,Biospan,TX,23,meaganbuckner@biospan.com,Buckner
854,Jimenez,603 Cooper Street,49795,F,Moscow,Verton,AL,25,jimenezbarry@verton.com,Barry
97,Karen,512 Cumberland Walk,49671,F,Fredericktown,Tsunamia,MO,40,karentrujillo@tsunamia.com,Trujillo
168,Carissa,975 Flatbush Avenue,49568,M,Neibert,Zillacom,IL,20,carissasimon@zillacom.com,Simon
240,Oconnor,659 Highland Boulevard,49741,F,Kilbourne,Franscene,NH,35,oconnorclay@franscene.com,Clay
803,Marissa,963 Highland Avenue,49567,M,Bloomington,Centregy,MS,25,marissaspears@centregy.com,Spears
248,West,717 Hendrickson Place,49989,M,Maury,Obliq,WA,36,westengland@obliq.com,England`,
  TextFile:
    `842|Meagan|F|Craig|49587|Biospan|TX|meaganbuckner@biospan.com|833 Bushwick Court|Buckner|23
854|Jimenez|F|Moscow|49795|Verton|AL|jimenezbarry@verton.com|603 Cooper Street|Barry|25
97|Karen|F|Fredericktown|49671|Tsunamia|MO|karentrujillo@tsunamia.com|512 Cumberland Walk|Trujillo|40
168|Carissa|M|Neibert|49568|Zillacom|IL|carissasimon@zillacom.com|975 Flatbush Avenue|Simon|20
240|Oconnor|F|Kilbourne|49741|Franscene|NH|oconnorclay@franscene.com|659 Highland Boulevard|Clay|35
803|Marissa|M|Bloomington|49567|Centregy|MS|marissaspears@centregy.com|963 Highland Avenue|Spears|25
248|West|M|Maury|49989|Obliq|WA|westengland@obliq.com|717 Hendrickson Place|England|36
`,
};