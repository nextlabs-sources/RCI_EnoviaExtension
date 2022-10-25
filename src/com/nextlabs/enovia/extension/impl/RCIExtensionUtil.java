package com.nextlabs.enovia.extension.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import matrix.util.StringList;

import org.apache.log4j.Logger;

import com.matrixone.apps.domain.util.MapList;
import com.nextlabs.enovia.extension.NextLabsUtil;
import com.rockwellcollins.classificationrepository.PartClassification;
import com.rockwellcollins.classificationrepository.PartClassificationItem;
import com.rockwellcollins.classificationrepository.PartContractItem;
import com.rockwellcollins.classificationrepository.PartLicenseItem;
import com.rockwellcollins.classificationrepository.PartsClassificationResult;

public class RCIExtensionUtil {
	
	private static RCIExtensionLogger logger = null;
	
	static {
		logger = new RCIExtensionLogger(Logger.getLogger("RCIEXTLOGGER"));
	}
	
	@SuppressWarnings("unchecked")
	public MapList setClassificationFault(MapList relatedParts, boolean fault) {
		if (relatedParts != null) {
			try {
				for (int i = 0; i < relatedParts.size(); i++) {
					Map<String, Object> map = (Map<String, Object>) relatedParts.get(i);
					
					map.put(RCIExtensionConstant.CLASSIFICATION_API_EXCEPTION, String.valueOf(fault));
				}
			} catch (Exception ex) {
				logger.error("RCIExtensionUtil setClassificationFault() caught exception: " + ex.getMessage(), ex);
			}
		}
		
		return relatedParts;
	}

	@SuppressWarnings("unchecked")
	public MapList setClassificationResult(MapList relatedParts, 
			PartsClassificationResult partsClassificationResult) {
		
		if (relatedParts != null) {
			
			logger.debug("RCIExtensionUtil setClassificationResult()");
			
			MapList relatedParts_classified = new MapList();
			PartClassification[] partClassifications = 
					partsClassificationResult.PartClassificationRecords;
			
			try {
				for (int i = 0; i < relatedParts.size(); i++) {
					Map<String, Object> map = (Map<String, Object>) relatedParts.get(i);
										
					// retrieving part number
					if (map.containsKey(RCIExtensionConstant.PART_NUMBER)) {
						for (PartClassification partClassification : partClassifications) {
							
							if (map.get(RCIExtensionConstant.PART_NUMBER).equals(partClassification.PartNumber)) {
							
								logger.debug("RCIExtensionUtil setClassificationResult() - partClassificationItems isNULL=" + 
										(partClassification.ClassificationItems == null));
								
								logger.debug("RCIExtensionUtil setClassificationResult() - partLicenseItems isNULL=" + 
										(partClassification.LicenseItems == null));
								
								logger.debug("RCIExtensionUtil setClassificationResult() - partContractItem isNULL=" + 
								(partClassification.ContractItems == null));
								
								PartClassificationItem[] partClassificationItems = 
										partClassification.ClassificationItems;
								
								PartLicenseItem[] partLicenseItems = 
										partClassification.LicenseItems;
								
								PartContractItem[] partContractItems =
										partClassification.ContractItems;
								
								// setting partLicenseItems
								logger.debug("RCIExtensionUtil setClassificationResult() - Setting partLicenseItems for " + 
										partClassification.PartNumber);
								
								StringBuffer strBuffer_license = new StringBuffer("");
								StringBuffer strBuffer_expired_license = new StringBuffer("");
								
								boolean gotLicenseItem = false;
								
								if (partLicenseItems != null) {
									for (PartLicenseItem partLicenseItem : 
										partLicenseItems) {
										
										strBuffer_license.append(partLicenseItem.LicenseNumber + ",");
										
										if (!isLicenseValid(partLicenseItem.ExpirationDate)) {
											strBuffer_expired_license.append(partLicenseItem.LicenseNumber + ",");
										}
											
										gotLicenseItem = true;
									}								
								}
								
								if (gotLicenseItem) {
									String licenseNumbers = strBuffer_license.substring(
											0, strBuffer_license.length() - 1);
									
									String expiredLicenseNumbers;
									if (strBuffer_expired_license.length() > 0) {
										expiredLicenseNumbers = strBuffer_expired_license.substring(
												0, strBuffer_expired_license.length() - 1);
									} else {
										expiredLicenseNumbers = "";
									}
									
									map.put(RCIExtensionConstant.LICENSE_NUMBER, licenseNumbers);
									logger.debug("RCIExtensionUtil setClassificationResult() - Setting key & value: " + 
											RCIExtensionConstant.LICENSE_NUMBER + "; " + licenseNumbers);
									
									map.put(RCIExtensionConstant.EXPIRED_LICENSE_NUMBER, expiredLicenseNumbers);
									logger.debug("RCIExtensionUtil setClassificationResult() - Setting key & value: " + 
											RCIExtensionConstant.EXPIRED_LICENSE_NUMBER + "; " + expiredLicenseNumbers);
								}
								
								logger.debug("RCIExtensionUtil setClassificationResult() - Setting partLicenseItems for " + 
										partClassification.PartNumber + " is complete");
								
								// setting partContractItems
								logger.debug("RCIExtensionUtil setClassificationResult() - Setting partContractItems for " + 
										partClassification.PartNumber);
								
								StringBuffer strBuffer_contract = new StringBuffer("");
								boolean gotContractItem = false;
								
								if (partContractItems != null) {
									for (PartContractItem partContractItem : partContractItems) {
										strBuffer_contract.append(partContractItem.ContractNumber + ",");
										gotContractItem = true;
									}
								}
								
								if (gotContractItem) {
									String contractNumbers = strBuffer_contract.substring(0, strBuffer_contract.length() - 1);
									
									map.put(RCIExtensionConstant.CONTRACT_NUMBER, contractNumbers);
									logger.debug("RCIExtensionUtil setClassificationResult() - Setting key & value: " + 
											RCIExtensionConstant.CONTRACT_NUMBER + "; " + contractNumbers);
								}
								
								logger.debug("RCIExtensionUtil setClassificationResult() - Setting partContractItems for " +
										partClassification.PartNumber + " is complete");
								
								// setting partClassificationItems
								logger.debug("RCIExtensionUtil setClassificationResult() - Setting partClassificationItems for " + 
										partClassification.PartNumber);
								
								int classificationItemCount = 0;
								if (partClassificationItems != null) {
									for (PartClassificationItem partClassificationItem : 
										partClassificationItems) {
										Map<String, Object> map_classified = new HashMap<String, Object>();
										// putting in the basic info likes valid structure, vault, 
										// licenseItem (if available), contractItem (if available)
										map_classified.putAll(map);
										
										/**
										 * use only when meets the filtering criteria
										 * i.e. country is US and regulation is TD
										 */
										logger.debug("RCIExtensionUtil setClassificationResult() - Classification data country: " + 
												partClassificationItem.Country);
										logger.debug("RCIExtensionUtil setClassificationResult() - Classification data regulation: " + 
												partClassificationItem.Regulation);
										if (partClassificationItem.Country.equalsIgnoreCase(RCIExtensionConstant.FILTER_COUNTRY) &&
											partClassificationItem.Regulation.equalsIgnoreCase(RCIExtensionConstant.FILTER_REGULATION)) {
											classificationItemCount++;
											
											/**
											 * if the value is null/empty, leave it out
											 */
											setMapKeyValuePair(map_classified, RCIExtensionConstant.MATERIAL_GROUP, 
													partClassificationItem.MaterialGroup);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.COUNTRY, 
													partClassificationItem.Country);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.REGULATION, 
													partClassificationItem.Regulation);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.ECCN, 
													partClassificationItem.ECCN);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.GROUPING, 
													partClassificationItem.Grouping);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.SCCODE, 
													partClassificationItem.SCCode);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.DESIGN_COG, 
													partClassificationItem.DesignCog);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.PDM_MASTER_VAULT, 
													partClassificationItem.PDMMasterVault);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.US_LEGAL_CONFIRMED, 
													partClassificationItem.US_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.UK_LEGAL_CONFIRMED, 
													partClassificationItem.UK_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.FR_LEGAL_CONFIRMED, 
													partClassificationItem.FR_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.DE_LEGAL_CONFIRMED, 
													partClassificationItem.DE_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.AU_LEGAL_CONFIRMED, 
													partClassificationItem.AU_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.CA_LEGAL_CONFIRMED, 
													partClassificationItem.CA_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.SG_LEGAL_CONFIRMED, 
													partClassificationItem.SG_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.NZ_LEGAL_CONFIRMED, 
													partClassificationItem.NZ_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.CN_LEGAL_CONFIRMED, 
													partClassificationItem.CN_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.JP_LEGAL_CONFIRMED, 
													partClassificationItem.JP_LegalConfirmed);
											
											setMapKeyValuePair(map_classified, RCIExtensionConstant.NL_LEGAL_CONFIRMED, 
													partClassificationItem.NL_LegalConfirmed);
											
											// only put the first classification with country & regulation
											if (classificationItemCount == 1) 
												relatedParts_classified.add(map_classified);
										} // end of if condition
									} // end of partClassificationItems for loop
								}
																
								logger.debug("RCIExtensionUtil setClassificationResult() - Setting partClassificationItems for " + 
										partClassification.PartNumber + " is complete");
								
								if (classificationItemCount > 1) {
									logger.error("RCIExtensionUtil setClassificationResult() - Has MORE THAN 1 classification for " + 
											partClassification.PartNumber);
								} else if (classificationItemCount == 0) {
									logger.debug("RCIExtensionUtil setClassificationResult() - No classification data for " + 
											partClassification.PartNumber);
									
									// putting in the basic info likes valid structure, vault, 
									// licenseItem (if available), contractItem (if available)
									Map<String, Object> map_classified = new HashMap<String, Object>();
									map_classified.putAll(map);
									
									relatedParts_classified.add(map_classified);
								} else {
									logger.debug("RCIExtensionUtil setClassificationResult() - Complete setting classification data for " + 
											partClassification.PartNumber);
								}
							}
						} // end of partClassifications for loop
					}
				} // end of relatedParts for loop
				
				relatedParts = relatedParts_classified;
			} catch (Exception ex) {
				logger.error("RCIExtensionUtil setClassificationResult() caught exception: " + ex.getMessage(), ex);
			}
		}
		
		return relatedParts;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void setMapKeyValuePair(Map map, String key, String value) {
		logger.debug("RCIExtensionUtil setting value for key: " + key);
		if (value != null && 
				!value.trim().equals("")) {
			map.put(key, value);
			logger.debug("RCIExtensionUtil setting key & value: " + key + "; " + value);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void printData(MapList data) {
		for (int i = 0; i < data.size(); i++) {
			Map<String, Object> map = (Map<String, Object>) data.get(i);
			
			logger.debug("RCIExtensionUtil printData() %d - %s", i, map);
		}
	}
	
	private boolean isLicenseValid(Date date) {
		Date currentDate = new Date();
	
		boolean validLicense = (currentDate.compareTo(date) > 0) ? false : true;
		logger.debug("RCIExtensionUtil isLicenseExpire() - " + 
				"Current date: " + currentDate.toString() + "; " + 
				"Expired date: " + date.toString() + "; " + 
				"Valid: " + validLicense);
		
		return validLicense;
	}
	
	public String convertStrListToStr(StringList list) {
		StringBuffer strBuffer = new StringBuffer("");
		
		for (int i = 0; i < list.size(); i++) {
			strBuffer.append(list.get(i) + ",");
		}
		
		if (strBuffer.length() <= 0)
			return strBuffer.toString().toLowerCase();
		else
			return strBuffer.substring(0, strBuffer.length() - 1).toLowerCase();
	}
	
	public static void loadPropertiesFromFile() {
		if (RCIExtensionConstant.HOST_NAME == null) {
			String sConfigPath = NextLabsUtil.getConfigPath();
			
			Properties properties = new Properties();
			
			try {
				properties.load(new FileInputStream(new File(sConfigPath + 
						RCIExtensionConstant.CLASSIFICATION_CONN_PROPERTIES)));
				
				RCIExtensionConstant.HOST_NAME = properties.getProperty("host_name");
				RCIExtensionConstant.CLIENT_ID = properties.getProperty("client_id");
				RCIExtensionConstant.USER_NAME = properties.getProperty("user_name");
				RCIExtensionConstant.PASSWORD = properties.getProperty("password");
				RCIExtensionConstant.ENCRYPTIONSECRETKEY = properties.getProperty("encryptionsecretkey");
				
				logger.debug("RCIExtensionUtil loadPropertiesFromFile() - HOST_NAME = " + RCIExtensionConstant.HOST_NAME);
				logger.debug("RCIExtensionUtil loadPropertiesFromFile() - CLIENT_ID = " + RCIExtensionConstant.CLIENT_ID);
				logger.debug("RCIExtensionUtil loadPropertiesFromFile() - USER_NAME = " + RCIExtensionConstant.USER_NAME);
				// password and encryption secret key are omitted
			} catch (IOException ioe) {
				logger.error("RCIExtensionUtil loadPropertiesFromFile() caught exception: " + ioe.getMessage(), ioe);
			}
		}
	}
	
	public String getEventId(String userId, long timestamp) {
		if (userId != null) {			
			if (userId.contains("@"))
				userId = userId.substring(0, userId.indexOf("@"));
			
			userId = userId.toLowerCase();
			userId = userId.replaceAll(" ", "");
			
			return userId + String.valueOf(timestamp); 
		} else {
			return String.valueOf(timestamp);
		}
	}
	
}
