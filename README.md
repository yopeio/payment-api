# Yope API

* It is currency-agnostic and blockchain-agnostic. 
* It exposes a RESTful API which can be consumed by any client program. Read our documentation here: developer.yope.com
* Transactions happen off-chain so there is no need to wait for confirmations and there is no limit to the number of transactions or internal wallets that a user can have. Writing to the blockchain occurs only when the user deposits or withdraws their Yope balance, while internal transactions between Yope wallets happen at the speed of the internet, rather than the speed of the blockchain. 
* Yope API authentication is performed through the blockchain, given that accounts, profiles and roles are registered in the blockchain, and as such are totally decentralised (coming soon)
* It can be locally installed in a matter of seconds, thanks to an up-to-date Docker image stored into the Hub registry (coming soon)
* Dockerisation makes it easy for any developer to be up and running writing smart contracts within 10 minutes, ready for integration into the Ethereum blockchain (coming soon)
* The Yope API is perfectly designed to buy or sell virtual goods and to register them as smart contracts.



## Minimum prerequisites 
* java 8
* Apache Maven v.3.0.3
* Neo4j v.2.3.0
* Redis v.2.8.12

## System requirements
* at least 2GB RAM
 
## How to run it locally
* clone the project and build it from the root executing the maven command `mvn clean install`
* run Neo4j executing `neo4j start` command and change the password in `localhost:7474` using the same `neo4jPassword` defined in `yope-payment-rest/application.yml`
* run Redis executing `redis-server` command
* run the project executing the maven command from the path `mvn spring-boot:run`
* test it executing the command `curl --include \
     --request POST \
     --header "Content-Type: application/json" \
     --data-binary "{
    \"email\": \"sellerB@yope.io\",
    \"firstName\": \"John\",
    \"lastName\": \"Bull\",
    \"name\": \"VEGA\",
    \"password\": \"pwd123\"
}" \
'http://localhost:8080/accounts'`. 

* the response should be like `{
  "header" : {
    "success" : true,
    "status" : 200
  },
  "body" : {
    "type" : "SELLER",
    "id" : 37,
    "email" : "sellerB@yope.io",
    "firstName" : "John",
    "lastName" : "Bull",
    "status" : "ACTIVE",
    "registrationDate" : 1451239489421
  }
}`
  
More infos: http://developer.yope.io

Documentation: http://docs.yope.apiary.io

Cookbook: https://github.com/yopeio/payment-api/blob/master/YD-Cookbook-291115-1530-187.pdf
