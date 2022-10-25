package com.nextlabs.enovia.extension.impl;

import java.util.ArrayList;

import com.rockwellcollins.classificationrepository.CitizenshipRequest;
import com.rockwellcollins.classificationrepository.CitizenshipResult;
import com.rockwellcollins.classificationrepository.ClassificationRepositoryAPI;
import com.rockwellcollins.classificationrepository.ClassificationRepositoryAPIException;
import com.rockwellcollins.classificationrepository.PartsClassificationRequest;
import com.rockwellcollins.classificationrepository.PartsClassificationResult;

public class RCIClassificationAgent {
	
	static {
		RCIExtensionUtil.loadPropertiesFromFile();
		
		// trigger ICassificationRepositoryAPI.getPartsClassification
		ArrayList<String> listCacheServers = new ArrayList<String>();
		listCacheServers.add(RCIExtensionConstant.HOST_NAME);
		
		ClassificationRepositoryAPI.SetCacheServers(listCacheServers);
	    ClassificationRepositoryAPI.ClientID = Integer.parseInt(RCIExtensionConstant.CLIENT_ID);
	    ClassificationRepositoryAPI.Username = RCIExtensionConstant.USER_NAME;
	    ClassificationRepositoryAPI.Password = RCIExtensionConstant.PASSWORD;
	    ClassificationRepositoryAPI.EncryptionSecretKey = RCIExtensionConstant.ENCRYPTIONSECRETKEY;
	}
	
	public RCIClassificationAgent() {
		
	}
	
	// return PartsClassificationResult from IClassificationRepositoryAPI
	public PartsClassificationResult getClassification(String[] partNumbers) 
			throws ClassificationRepositoryAPIException {
		// create PartsClassificaitonRequest
		PartsClassificationRequest classificationRequest = new PartsClassificationRequest();
		classificationRequest.PartNumbers = partNumbers;

        PartsClassificationResult partClassificationResult = 
        		ClassificationRepositoryAPI.getPartsClassification(classificationRequest);
        
		return partClassificationResult;
	}
	
	// return CitizenshipResult from IClassificationRepositoryAPI
	public CitizenshipResult getCitizenshipResult(String username)
			throws ClassificationRepositoryAPIException {
		// if request if from web query tool (user is web query admin)
		// don't query classification repository, return default value
		if (username.equalsIgnoreCase(RCIExtensionConstant.WQ_USERNAME)) {
			CitizenshipResult result = new CitizenshipResult();
			
			result.IsConfirmed = RCIExtensionConstant.WQ_CITIZENSHIP_CONFIRMED_DEFAULT_VALUE;
			result.ResultCode = RCIExtensionConstant.CITIZENSHIP_CONFIRMED_SUCCESS;
			
			return result;
		} else {
			CitizenshipRequest request = new CitizenshipRequest();
			request.Username = username;
		
			CitizenshipResult result = ClassificationRepositoryAPI.getCitizenship(request);
		
			return result;
		}
	}
	
}
