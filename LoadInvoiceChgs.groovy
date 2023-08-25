/*
 ***************************************************************
 *                                                             *
 *                           NOTICE                            *
 *                                                             *
 *   THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS             *
 *   CONFIDENTIAL INFORMATION OF INFOR AND/OR ITS AFFILIATES   *
 *   OR SUBSIDIARIES AND SHALL NOT BE DISCLOSED WITHOUT PRIOR  *
 *   WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND       *
 *   ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH  *
 *   THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.            *
 *   ALL OTHER RIGHTS RESERVED.                                *
 *                                                             *
 *   (c) COPYRIGHT 2020 INFOR.  ALL RIGHTS RESERVED.           *
 *   THE WORD AND DESIGN MARKS SET FORTH HEREIN ARE            *
 *   TRADEMARKS AND/OR REGISTERED TRADEMARKS OF INFOR          *
 *   AND/OR ITS AFFILIATES AND SUBSIDIARIES. ALL RIGHTS        *
 *   RESERVED.  ALL OTHER TRADEMARKS LISTED HEREIN ARE         *
 *   THE PROPERTY OF THEIR RESPECTIVE OWNERS.                  *
 *                                                             *
 ***************************************************************
 */

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *ABF_R_0625        20220405  RDRIESSEN   Mods BF0625- Generate APS450 invoice printout extension workfile
 *ABF_R_0625        20230825  RDRIESSEN   Mods BF0625- troubleshooted., clear one variable    
 *
 */


 import groovy.lang.Closure;
 
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

public class LoadInvoiceChgs extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String cono;
  private String divi;
  private String bref;
  private String suno;
  private String sudo;  
  private String sunm;
  private String rgtm;  
  private String tmsx;
  private String bins; 
  private String atnr;   
  private String atid; 
  private String atva;
  private String tx30; 
  private String tx15;
  private String tx31;
  private String dfa1;
  private String name;
  private String whlo;
  private String rgdt;
  private String grwe;
  private String ridn;
  private String ridl;
  private String ref3;
  private String ref4;
  private String ovhe;
  private String ovhem;
  private String mstx;
  private String ovhex; 
  private String ovhey;  
  private String bano;
  private String itno;
  private String puno;
  private String pnli;
  private String atnb; 
  private String grad;
  private String car1;
  private String loa1;
  private String itty;
  private String itgr;
  private String tawe;
  private String newe;
  private String trdt;
  
  
  private List lstQITests_Range;
  private List lstQITests_Target;
  private List lstQITests_Quality;
  private List lstTestResults_Range;
  private List lstSortedTestResults_Range;
  private List lstTestResults_Target;
  private List lstSortedTestResults_Target;
  private List lstTestResults_Quality;
  private List lstSortedTestResults_Quality;
  private List lstTestResults_Range01;
  private List lstSortedTestResults_Range01;
  private List lstTestResults_Target01;
  private List lstSortedTestResults_Target01;
  private List lstTestResults_Quality01;
  private List lstSortedTestResults_Quality01;
  
  private int XXCONO;
  private boolean found;
 
  public LoadInvoiceChgs(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
	}
  
  public void main() {
    
    cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();
    if (cono == "?") {
      cono = "";
    } 
    whlo = mi.inData.get("WHLO") == null ? '' : mi.inData.get("WHLO").trim();
    if (whlo == "?") {
      whlo = "";
    } 
    itno = mi.inData.get("ITNO") == null ? '' : mi.inData.get("ITNO").trim();
    if (itno == "?") {
      itno = "";
    }
    rgdt = mi.inData.get("RGDT") == null ? '' : mi.inData.get("RGDT").trim();
    if (rgdt == "?") {
      rgdt = "";
    }	
  	
    rgtm = mi.inData.get("RGTM") == null ? '' : mi.inData.get("RGTM").trim();
    if (rgtm == "?") {
      rgtm = "";
    } 
  	
    tmsx = mi.inData.get("TMSX") == null ? '' : mi.inData.get("TMSX").trim();
    if (tmsx == "?") {
      tmsx = "";
    } 
    
    trdt = mi.inData.get("TRDT") == null ? '' : mi.inData.get("TRDT").trim();
    if (trdt == "?") {
      trdt = "";
    }
    
    bano = mi.inData.get("BANO") == null ? '' : mi.inData.get("BANO").trim();
    if (bano == "?") {
      bano = "";
    } 
  	
    ridn = mi.inData.get("RIDN") == null ? '' : mi.inData.get("RIDN").trim();
    if (ridn == "?") {
      ridn = "";
    } 
  	
    ridl = mi.inData.get("RIDL") == null ? '' : mi.inData.get("RIDL").trim();
    if (ridl == "?") {
      ridl = "";
    } 
  	
    sudo = mi.inData.get("SUDO") == null ? '' : mi.inData.get("SUDO").trim();
    if (sudo == "?") {
      sudo = "";
    } 
  	
    atnb = mi.inData.get("ATNB") == null ? '' : mi.inData.get("ATNB").trim();
    if (atnb == "?") {
      atnb = "";
    } 
  	
    suno = "";
    grwe = "0";
    tawe = "0";
    newe = "0";
    ovhe = "0";
    ovhex = "0";
    ovhem = "0";
    ovhey = "0";
    car1 = "";
    loa1 = "";
  
    if (rgdt.isEmpty()) { rgdt = "0";  }
    if (rgtm.isEmpty()) { rgtm = "0";  }
    if (tmsx.isEmpty()) { tmsx = "0";  }
    if (ridl.isEmpty()) { ridl = "0";  }
    if (atnb.isEmpty()) { atnb = "0";  }
    if (trdt.isEmpty()) { trdt = "0";  }
  
    grwe = "0";
    
    //Validate input fields
    if (!validateInput()) {
      return;
    }
  
    deleteFromEXTCHG(cono, whlo, itno, rgdt, rgtm, mstx);
  	
    // carrier fees
    writeEXTCHG0101(cono, whlo, itno, rgdt, rgtm, mstx, bano, ridn, ridl, sudo, atnb, trdt);

    // loader fees  	   
    writeEXTCHG0102(cono, whlo, itno, rgdt, rgtm, mstx, bano, ridn, ridl, sudo, atnb, trdt);
  	   
    // DFA fees   
    writeEXTCHG0103(cono, whlo, itno, rgdt, rgtm, mstx, bano, ridn, ridl, sudo, atnb, trdt);
  }
  
  /*
  * validateInput - Validate all the input fields - replicated from PECHK() of CRS575
  * @return false if there is any error
  *         true if pass the validation
  */
  boolean validateInput() { 
    if (!cono.isEmpty()) {
	    if (cono.isInteger()){
		    XXCONO= cono.toInteger();
			  } else {
				  mi.error("Company " + cono + " is invalid.");
				  return false;
			  }
		  } else {
			XXCONO= program.LDAZD.CONO;
	  }
    if (whlo.isEmpty()) {
      mi.error("Warehouse must be entered.");
      return false;
    }
    // - Warehouse
    DBAction queryMITWHL = database.table("MITWHL").index("00").selection("MWDIVI","MWFACI").build();
    DBContainer MITWHL = queryMITWHL.createContainer();
    MITWHL.set("MWCONO", XXCONO);
    MITWHL.set("MWWHLO", whlo);
    
    if (!queryMITWHL.read(MITWHL)){
      mi.error("Warehouse dose not exist in MITWHL.");
      return false;
    }
    if (itno.isEmpty()) {
      mi.error("Item number must be entered.");
      return false;
    }
    // - item master
    DBAction queryMITMAS = database.table("MITMAS").index("00").selection("MMITDS").build();
    DBContainer MITMAS = queryMITMAS.getContainer();
    MITMAS.set("MMCONO", XXCONO);
    MITMAS.set("MMITNO", itno);
    if (!queryMITMAS.read(MITMAS)){
      mi.error("Item number does not exist in MITMAS.");
      return false; 
    }
    if (rgdt.toInteger() == 0) {
      mi.error("Entry must be entered.");
      return false;
    }
    if (!isDateValid(rgdt)) {
      mi.error("Entry date is not a valid date");
      return;
    }
    if (rgtm.toInteger() == 0) {
      mi.error("Entry time must be entered.");
      return false;
    }
    if (tmsx.toInteger() == 0) {
      mi.error("Time suffix must be entered.");
      return false;
    }
    if (bano.isEmpty()) {
      mi.error("Lot number must be entered.");
      return false;
    }
    // - mittra
    DBAction queryMITTRA = database.table("MITTRA").index("00").selection("MTATNB", "MTSUDO").build();
    DBContainer MITTRA = queryMITTRA.getContainer();
    MITTRA.set("MTCONO", XXCONO);
		MITTRA.set("MTWHLO", whlo);
		MITTRA.set("MTITNO", itno);
		MITTRA.set("MTRGDT", rgdt.toInteger());
		MITTRA.set("MTRGTM", rgtm.toInteger());
		MITTRA.set("MTTMSX", tmsx.toInteger());
		if (!queryMITTRA.read(MITTRA)) {
		  mi.error("Record does not exist in MITTRA.");
      return false;
    } 
    if (!atnb.isEmpty()) {
      String atnb_MITTRA = MITTRA.get("MTATNB").toString().trim();
      if (atnb.toLong() != atnb_MITTRA.toLong()) {
        mi.error("Attribute number is invalid.");
        return false;
      }
    }
    
    if (!sudo.isEmpty()) {
      String sudo_MITTRA = MITTRA.get("MTSUDO").toString().trim();
      if (sudo != sudo_MITTRA) {
        mi.error("Delivery no is invalid.");
        return false;
      }
    }
    
    // - lot master
    if (!bano.isEmpty()) {
      DBAction queryMILOMA = database.table("MILOMA").index("00").selection("LMBANO").build();
      DBContainer MILOMA = queryMILOMA.getContainer();
      MILOMA.set("LMCONO", XXCONO);
      MILOMA.set("LMITNO", itno);
      MILOMA.set("LMBANO", bano);
      if (!queryMILOMA.read(MILOMA)){
        mi.error("Lot number does not exist in MILOMA.");
        return false;
      }
    }
    // - purchase order line
    if (!ridn.isEmpty() && ridl.toInteger() != 0) {
      DBAction queryMPLINE = database.table("MPLINE").index("00").selection("IBPNLI").build();
      DBContainer MPLINE = queryMPLINE.getContainer();
      MPLINE.set("IBCONO", XXCONO);
      MPLINE.set("IBPUNO", ridn);
      MPLINE.set("IBPNLI", ridl.toInteger());
      MPLINE.set("IBPNLS", 0);
      if (!queryMPLINE.read(MPLINE)){
        mi.error("Purchase order line does not exist.");
        return false;
      }
    }
   
    if (trdt.toInteger() != 0 && !isDateValid(trdt)) {
      mi.error("Transaction date is invalid.");
      return false;
    }
    return true;
  }
  /**
   * isDateValid - check if input string is a valid date
   *  - date format: yyyyMMdd
   * return boolean
   */
  def isDateValid(String dateStr) {
    boolean dateIsValid = true;
    Matcher matcher=
      Pattern.compile("^((2000|2400|2800|(19|2[0-9](0[48]|[2468][048]|[13579][26])))0229)\$" 
        + "|^(((19|2[0-9])[0-9]{2})02(0[1-9]|1[0-9]|2[0-8]))\$"
        + "|^(((19|2[0-9])[0-9]{2})(0[13578]|10|12)(0[1-9]|[12][0-9]|3[01]))\$" 
        + "|^(((19|2[0-9])[0-9]{2})(0[469]|11)(0[1-9]|[12][0-9]|30))\$").matcher(dateStr);
    dateIsValid = matcher.matches();
    if (dateIsValid) {
      dateIsValid = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd")) != null;
    } 
    return dateIsValid;
  }
 
  /*
	 * deleteEXTCHG - delete records from EXTCHG
	 *
	*/
  def deleteFromEXTCHG(String cono, String whlo, String itno, String rgdt, String rgtm, String mstx) {
    
    DBAction queryEXTCHG = database.table("EXTCHG").index("00").selection("EXCONO", "EXWHLO", "EXITNO", "EXRGDT", "EXRGTM", "EXTMSX").build();
    DBContainer EXTCHG = queryEXTCHG.getContainer();
    EXTCHG.set("EXCONO", XXCONO);
    EXTCHG.set("EXWHLO", whlo);
    EXTCHG.set("EXITNO", itno);
    EXTCHG.set("EXRGDT", rgdt.toInteger());
    EXTCHG.set("EXRGTM", rgtm.toInteger());
    EXTCHG.set("EXTMSX", tmsx.toInteger());
    queryEXTCHG.readAllLock(EXTCHG, 6, deleteEXTCHG);
  }
  
  /*
  * deleteEXTCHG - Callback function
  *
  */
  Closure<?> deleteEXTCHG = { LockedResult EXTCHG ->
    EXTCHG.delete();
  }
  
  /*
  * writeEXTCHG0101 - write carrier fees
  *
  */
  def writeEXTCHG0101(String cono, String whlo, String itno, String rgdt, String rgtm, String mstx, String bano, String ridn, String ridl, String sudo, String atnb, String trdt) {
  
    def params01 = ["ATNR":atnb.toString()]; 
    
    def callback01 = {
      Map<String, String> response ->
    
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC01") && response.ATVN != null){
          grwe =  response.ATVN;   
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC02") && response.ATVN != null){
          tawe =  response.ATVN;
          newe = grwe.toDouble() - tawe.toDouble();
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("SUP01") && response.ATVA != null){
          suno =  response.ATVA;   
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC04") && response.ATVA != null){
          car1 =  response.ATVA;   
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC05") && response.ATVA != null){
          loa1 =  response.ATVA;   
        }
      }
    }
   
   	miCaller.call("ATS101MI","GetAttributes", params01, callback01);
    
    def params02 = ["CEID": "CAR01", "OVK1":"99999", "OVK2":suno.toString(),]; 
    
    def callback02 = {
      Map<String, String> response ->
      if(response.OVHE != null){
        ovhe =  response.OVHE;   
      }
    }
    
	  miCaller.call("PPS280MI","LstElementValue", params02, callback02);

    ovhex = ovhe.toDouble() * newe.toDouble();

   	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
    if(car1.trim() != '99999')  {
    
  	 // write carrier fees
  	  DBAction actionEXTCHG = database.table("EXTCHG").build();
  	  DBContainer EXTCHG = actionEXTCHG.getContainer();
  	  EXTCHG.set("EXCONO", XXCONO);
  	  EXTCHG.set("EXWHLO", whlo);
  	  EXTCHG.set("EXRGDT", rgdt.toInteger());
  	  EXTCHG.set("EXTRDT", trdt.toInteger());
  	  EXTCHG.set("EXRGTM", rgtm.toInteger());
   	  EXTCHG.set("EXTMSX", tmsx.toInteger());
  	  EXTCHG.set("EXSUDO", sudo);
  	  EXTCHG.set("EXCHTP", 'Carrier Fees');
  	  EXTCHG.set("EXSUNO", suno);
  	  EXTCHG.set("EXITNO", itno);
  	  EXTCHG.set("EXBANO", bano);
  	  EXTCHG.set("EXLOTS", 1);
  	  EXTCHG.set("EXPUNO", ridn);
  	  EXTCHG.set("EXPNLI", ridl.toInteger());
  	  EXTCHG.set("EXWGHT", newe.toDouble());
  	  EXTCHG.set("EXRATE", ovhe.toDouble());
  	  EXTCHG.set("EXLNAM", ovhex.toDouble());
  	  EXTCHG.set("EXCAR1", car1);
  	  EXTCHG.set("EXLOA1", loa1);
  	  EXTCHG.set("EXPROC", '0');
  	  EXTCHG.set("EXCHID", program.getUser());
  	  actionEXTCHG.insert(EXTCHG, recordExists);
    }
  }
  
  /*
  * writeEXTCHG0102 - write loader fees
  *
  */
  def writeEXTCHG0102(String cono, String whlo, String itno, String rgdt, String rgtm, String mstx, String bano, String ridn, String ridl, String sudo, String atnb, String trdt) {
  
    def params01 = ["ATNR":atnb.toString()]; 
    
    def callback01 = {
      Map<String, String> response ->
      
    if(response.ATID != null){
      if(response.ATID.trim().equals("REC01") && response.ATVN != null){
        grwe =  response.ATVN;   
      }
    }
      
    if(response.ATID != null){
        
      if(response.ATID.trim().equals("REC02") && response.ATVN != null){
        tawe =  response.ATVN;
        newe = grwe.toDouble() - tawe.toDouble();
      }
    }
      
    if(response.ATID != null){
      if(response.ATID.trim().equals("SUP01") && response.ATVA != null){
        suno =  response.ATVA;   
      }
    }
      
    if(response.ATID != null){
      if(response.ATID.trim().equals("REC04") && response.ATVA != null){
        car1 =  response.ATVA;   
      }
    }
      
    if(response.ATID != null){
      if(response.ATID.trim().equals("REC05") && response.ATVA != null){
          loa1 =  response.ATVA;   
        }
      }
    }
   
   	miCaller.call("ATS101MI","GetAttributes", params01, callback01);
    
    if(car1.trim() == loa1.trim())  { 
      ovhem = "4"; 
    }
    if(loa1.trim() == "99998") { 
      ovhem = "2"; 
    }
    
    ovhey = ovhem.toDouble() * newe.toDouble();

   	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
    if(loa1.trim() != '99999')  {
   
   	  DBAction actionEXTCHG = database.table("EXTCHG").build();
  	  DBContainer EXTCHG = actionEXTCHG.getContainer();
  	  EXTCHG.set("EXCONO", XXCONO);
  	  EXTCHG.set("EXWHLO", whlo);
  	  EXTCHG.set("EXRGDT", rgdt.toInteger());
  	  EXTCHG.set("EXTRDT", trdt.toInteger());
  	  EXTCHG.set("EXRGTM", rgtm.toInteger());
   	  EXTCHG.set("EXTMSX", tmsx.toInteger());
  	  EXTCHG.set("EXSUDO", sudo);
  	  EXTCHG.set("EXCHTP", 'Loader Fees');
  	  EXTCHG.set("EXSUNO", suno);
  	  EXTCHG.set("EXITNO", itno);
  	  EXTCHG.set("EXBANO", bano);
  	  EXTCHG.set("EXLOTS", 1);
  	  EXTCHG.set("EXPUNO", ridn);
  	  EXTCHG.set("EXPNLI", ridl.toInteger());
  	  EXTCHG.set("EXWGHT", newe.toDouble());
  	  EXTCHG.set("EXRATE", ovhem.toDouble());
  	  EXTCHG.set("EXLNAM", ovhey.toDouble());
  	  EXTCHG.set("EXCAR1", car1);
  	  EXTCHG.set("EXLOA1", loa1);
  	  EXTCHG.set("EXPROC", '0');
  	  EXTCHG.set("EXCHID", program.getUser());
  	  actionEXTCHG.insert(EXTCHG, recordExists);
    }
  }
 
   /*
  * writeEXTCHG0101 - write DFA fees
  *
  */
  def writeEXTCHG0103(String cono, String whlo, String itno, String rgdt, String rgtm, String mstx, String bano, String ridn, String ridl, String sudo, String atnb, String trdt) {
  
    ovhe = 0;
  
    def params01 = ["ATNR":atnb.toString()];
    
    def callback01 = {
      Map<String, String> response ->
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC01") && response.ATVN != null){
          grwe =  response.ATVN;   
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC02") && response.ATVN != null){
          tawe =  response.ATVN;
          newe = grwe.toDouble() - tawe.toDouble();
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("SUP01") && response.ATVA != null){
          suno =  response.ATVA;   
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC04") && response.ATVA != null){
          car1 =  response.ATVA;   
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC05") && response.ATVA != null){
          loa1 =  response.ATVA;   
        }
      }
    
      if(response.ATID != null){
        if(response.ATID.trim().equals("REC11") && response.ATVA != null){
          dfa1 =  response.ATVA.trim();   
        }
      }
      
      if(response.ATID != null){
        if(response.ATID.trim().equals("ITM01") && response.ATVA != null){
          itno =  response.ATVA;   
        }
      }
    }
   
   	miCaller.call("ATS101MI","GetAttributes", params01, callback01);
    
    // get item parameters
    
    def params04 = ["ITNO": itno.toString(),  ]; // toString is needed to convert from gstring to string
    
    def callback04 = {
      Map<String, String> response ->
      if(response.OVHE != null){
        itty =  response.ITTY; 
        itgr =  response.ITGR;
      }
    }
    
  	miCaller.call("MMS200MI","Get", params04, callback04);
    
    
    def params03 = ["CEID": "MBR01", "OVK1":suno.toString(), "OVK2":"Y",];
    
    def callback03 = {
      Map<String, String> response ->
      if(response.OVHE != null){
        ovhe =  response.OVHE;   
      }
    }
    
	  miCaller.call("PPS280MI","LstElementValue", params03, callback03);
    
    ovhex = ovhe.toDouble() * newe.toDouble();

   	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
    if(dfa1 != '99999')  {
   
   	  DBAction actionEXTCHG = database.table("EXTCHG").build();
  	  DBContainer EXTCHG = actionEXTCHG.getContainer();
  	  EXTCHG.set("EXCONO", XXCONO);
  	  EXTCHG.set("EXWHLO", whlo);
  	  EXTCHG.set("EXRGDT", rgdt.toInteger());
  	  EXTCHG.set("EXTRDT", trdt.toInteger());
  	  EXTCHG.set("EXRGTM", rgtm.toInteger());
   	  EXTCHG.set("EXTMSX", tmsx.toInteger());
  	  EXTCHG.set("EXSUDO", sudo);
  	  EXTCHG.set("EXCHTP", 'DFA Member Fees');
  	  EXTCHG.set("EXSUNO", suno);
  	  EXTCHG.set("EXITNO", itno);
  	  EXTCHG.set("EXBANO", bano);
  	  EXTCHG.set("EXLOTS", 1);
  	  EXTCHG.set("EXPUNO", ridn);
  	  EXTCHG.set("EXPNLI", ridl.toInteger());
  	  EXTCHG.set("EXWGHT", newe.toDouble());
  	  EXTCHG.set("EXRATE", ovhe.toDouble());
  	  EXTCHG.set("EXLNAM", ovhex.toDouble());
  	  EXTCHG.set("EXCAR1", dfa1);
  	  EXTCHG.set("EXLOA1", loa1);
  	  EXTCHG.set("EXPROC", '0');
  	  EXTCHG.set("EXCHID", program.getUser());
  	  actionEXTCHG.insert(EXTCHG, recordExists);
    }
  }
 
 /*
  * recordEXists - throw error if record exists
  *
  */
  Closure recordExists = {
	  mi.error("Record already exists");
  }
}
