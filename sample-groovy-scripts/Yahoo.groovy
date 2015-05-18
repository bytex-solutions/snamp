import groovyx.net.http.RESTClient

type INT32

yahooClient = new RESTClient('http://finance.yahoo.com/')

def getValue(){
    def response = yahooClient.get(path: 'webservice/v1/symbols/allcurrencies/quote', query: [format: "json"])
    return response.data.list.meta.count
}