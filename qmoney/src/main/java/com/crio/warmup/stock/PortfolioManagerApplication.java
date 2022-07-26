
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.Root;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_REST_API
  //  CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>
  
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objectmapper = getObjectMapper();  
    File inputFile = resolveFileFromResources(args[0]); 
    PortfolioTrade[] portfolioTrades = objectmapper.readValue(inputFile, PortfolioTrade[].class);
    List<TotalReturnsDto> trdto = new ArrayList<>(); 
    for(int i=0;i< portfolioTrades.length;i++){
      String url = "https://api.tiingo.com/tiingo/daily/"+portfolioTrades[i].getSymbol()+"/prices?startDate="+portfolioTrades[i].getPurchaseDate()+"&endDate="+args[1]+"&token=d024f195f4bf14d2e6975d6641266bfa5cd2e7d0"; 
      RestTemplate restTemplate = new RestTemplate(); 
      TiingoCandle[] tiingoCandle =  restTemplate.getForObject(url, TiingoCandle[].class);   
      int last = tiingoCandle.length-1;  
      TotalReturnsDto totalReturns = new TotalReturnsDto(null,0.0);  
       totalReturns.setClosingPrice(tiingoCandle[last].getClose()); 
       totalReturns.setSymbol(portfolioTrades[i].getSymbol()); 
       trdto.add(totalReturns); 

    }  
    Collections.sort(trdto, new Comparator<TotalReturnsDto>() {

      @Override
      public int compare(TotalReturnsDto arg0, TotalReturnsDto arg1) {
        
        if(arg0.getClosingPrice()==arg1.getClosingPrice()) 
          return 0; 
        else if(arg0.getClosingPrice()>arg1.getClosingPrice()) 
          return 1; 
        return -1;  
        
      }
    });
    List<String> sortedSymbol = new ArrayList<>();  
    List<String> prices = new ArrayList<>();
    for(TotalReturnsDto tr : trdto){
      sortedSymbol.add(tr.getSymbol());  
      //prices.add(tr.getClosingPrice().toString());
      
    } 
  
    
     return sortedSymbol;
  } 

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException, JsonMappingException, JsonParseException {
    ObjectMapper object = getObjectMapper();  
    File inputFile = resolveFileFromResources(args[0]); 
    List<String> trades = object.readValue(inputFile,List.class); 
    ObjectMapper objectmapper = getObjectMapper();    
    PortfolioTrade[] portfolioTrades = objectmapper.readValue(inputFile, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<>(); 
    for(PortfolioTrade r : portfolioTrades){
      symbols.add(r.getSymbol());
    }
    return symbols; 
    
  }  

  

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  } 
  
  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }
  
  
  
  public static List<String> debugOutputs() {
    String valueOfArgument0 = "trades.json";
       String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/samridh863-ME_QMONEY/qmoney/bin/main/trades.json";
       String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@2f9f7dcf";
       String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
       String lineNumberFromTestFileInStackTrace = "22"; 
       return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  } 

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {   
        ObjectMapper objectmapper = getObjectMapper();  
        File inputFile = resolveFileFromResources(args[0]); 
        PortfolioTrade[] portfolioTrades = objectmapper.readValue(inputFile, PortfolioTrade[].class);
        List<AnnualizedReturn> annualList = new ArrayList<>();   
        for(int i=0;i< portfolioTrades.length;i++){
          String url = "https://api.tiingo.com/tiingo/daily/"+portfolioTrades[i].getSymbol()+"/prices?startDate="+portfolioTrades[i].getPurchaseDate()+"&endDate="+args[1]+"&token=d024f195f4bf14d2e6975d6641266bfa5cd2e7d0"; 
          RestTemplate restTemplate = new RestTemplate(); 
          TiingoCandle[] tiingoCandle =  restTemplate.getForObject(url, TiingoCandle[].class);   
          int last = tiingoCandle.length-1;  
          AnnualizedReturn annual = calculateAnnualizedReturns(LocalDate.parse(args[1]), portfolioTrades[i], tiingoCandle[0].getOpen(), tiingoCandle[last].getClose());
          annualList.add(annual);


        } 
        Collections.sort(annualList , new Comparator<AnnualizedReturn>() {

              @Override
              public int compare(AnnualizedReturn arg0, AnnualizedReturn arg1) {
                
                if(arg0.getAnnualizedReturn()==arg1.getAnnualizedReturn()) 
                  return 0; 
                else if(arg0.getAnnualizedReturn()>arg1.getAnnualizedReturn()) 
                  return -1; 
                return 1;
              }
        });

      
      
        
        return annualList;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double totalReturn = (sellPrice-buyPrice)/buyPrice; 
      LocalDate startDate = trade.getPurchaseDate(); 
      Double years = ((double)ChronoUnit.DAYS.between(startDate,endDate))/365.0; 
      Double annualizedResturns = Math.pow((1+totalReturn), (1.0/years))-1.0; 
      AnnualizedReturn anRet = new AnnualizedReturn(trade.getSymbol(),annualizedResturns,totalReturn);
      return  anRet;
  }

















  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
      
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper(); 
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(file, PortfolioTrade[].class);
       RestTemplate restTemplate = new RestTemplate();
       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate); 
      }
  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  private static String readFileAsString(String file) throws URISyntaxException, IOException {
    return new String(Files.readAllBytes(resolveFileFromResources(file).toPath()), "UTF-8");
  }
  
  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
    



  }
}

