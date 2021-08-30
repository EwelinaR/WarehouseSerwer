## Warehouse server
Server side of warehouse management application. It stores data about instruments in the warehouse. 
Authorized users can use REST API to modify this data.

Client app is written on Android in Java. [Here](https://github.com/EwelinaR/WarehouseUser) you can find the code.

### Technologies / libraries
- oauth2 - implemented two levels of authentication (employee and manager)
- REST API - CRUD for instruments data
- H2 database
