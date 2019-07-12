package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@PluginImplementation
@Data
@Log4j
public class ValidateMetadataFieldsPlugin implements IStepPluginVersion2 {

    private Step step;
    private Process process;

    private PluginGuiType pluginGuiType = PluginGuiType.NONE;
    private String pagePath;
    private PluginType type = PluginType.Step;

    private String title = "intranda_step_validateMetadataFields";
    private List<MetadataMappingObject> metadataList = new ArrayList<>();
    
    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        this.process = step.getProzess();
        this.pagePath = returnPath;

        String projectName = step.getProzess().getProjekt().getTitel();

        XMLConfiguration xmlConfig = ConfigPlugins.getPluginConfig(title);
        xmlConfig.setExpressionEngine(new XPathExpressionEngine());
        xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
        SubnodeConfiguration config = null;
        // order of configuration is:
        //        1.) project name and step name matches
        //        2.) step name matches and project is *
        //        3.) project name matches and step name is *
        //        4.) project name and step name are *
        try {
            config = xmlConfig.configurationAt("//config[./project = '" + projectName + "'][./step = '" + step.getTitel() + "']");
        } catch (IllegalArgumentException e) {
            try {
                config = xmlConfig.configurationAt("//config[./project = '*'][./step = '" + step.getTitel() + "']");
            } catch (IllegalArgumentException e1) {
                try {
                    config = xmlConfig.configurationAt("//config[./project = '" + projectName + "'][./step = '*']");
                } catch (IllegalArgumentException e2) {
                    config = xmlConfig.configurationAt("//config[./project = '*'][./step = '*']");
                }
            }
        }
        
        List<HierarchicalConfiguration> mml = config.configurationsAt("//metadata");
        for (HierarchicalConfiguration md : mml) {
            metadataList.add(getMetadata(md));
        }
    }
    
    @Override
    public PluginReturnValue run() {
        boolean valid = true;
        List<String> issues = new ArrayList<>();
        
		try {
			// open the mets file
			Thread.sleep(5000);
			Fileformat ff = step.getProzess().readMetadataFile();
			DigitalDocument dd = ff.getDigitalDocument();
		    DocStruct doc = dd.getLogicalDocStruct();
		    // run through all metadata fields
		    for (Metadata m : doc.getAllMetadata()) {
		    	String title = m.getType().getName();
		    	String label_en = m.getType().getLanguage("en");
				String value = m.getValue();
				for (MetadataMappingObject mmo : metadataList) {
					if (mmo.getRulesetName().equals(title)) {
					
						// check if value is empty but required
						if (mmo.isRequired()) {
				            if (value == null || value.isEmpty()) {
				            	valid = false;
				                issues.add(label_en + ": " + mmo.getRequiredErrorMessage());
				            }
				        }
				        // check if value matches the configured pattern
				        if (mmo.getPattern() != null && value != null && !value.isEmpty()) {
				            Pattern pattern = mmo.getPattern();
				            Matcher matcher = pattern.matcher(value);
				            if (!matcher.find()) {
				            	valid = false;
				                issues.add(label_en + ": " + mmo.getPatternErrorMessage());
				            }
				        }
				        // checks whether all parts of value are in the list of controlled contents
				        if (!(mmo.getValidContent().isEmpty() || value == null || value.isEmpty())) {
				            String[] valueList = value.split("; ");
				            for (String v : valueList) {
				                if (!mmo.getValidContent().contains(v)) {
				                	valid = false;
					                issues.add(label_en + ": " + mmo.getValidContentErrorMessage());        
				                }
				            }
				        }
				        // check if a configured requirement of either field having content is
				        // fulfilled
	//			        if (!mmo.getEitherHeader().isEmpty()) {
	//			            if (rowMap.get(headerOrder.get(mmo.getEitherHeader())).isEmpty() && value.isEmpty()) {
	//			                datum.setValid(false);
	//			                datum.getErrorMessages().add(mmo.getEitherErrorMessage());
	//			            }
	//			        }
				        // check if field has content despite required field not having content
	//			        if (!mmo.getRequiredHeaders()[0].isEmpty()) {
	//			            for (String requiredHeader : mmo.getRequiredHeaders()) {
	//			                if (rowMap.get(headerOrder.get(requiredHeader)).isEmpty() && !value.isEmpty()) {
	//			                    datum.setValid(false);
	//			                    if (!datum.getErrorMessages().contains(mmo.getRequiredHeadersErrormessage())) {
	//			                        datum.getErrorMessages().add(mmo.getRequiredHeadersErrormessage());
	//			                    }
	//			                }
	//			            }
	//			        }
				        //check if field has the demanded wordcount
				        if (mmo.getWordcount() != 0) {
				            String[] wordArray = value.split(" ");
				            if (wordArray.length < mmo.getWordcount()) {
				            	valid = false;
				                issues.add(label_en + ": " + mmo.getWordcountErrormessage());
				            }
				        }
					}
				}
			}
		    
		} catch (ReadException | PreferencesException | WriteException | IOException | InterruptedException | SwapException | DAOException e1) {
          log.error("Cannot read all metadata from METS file. Validation canceled");
          return PluginReturnValue.ERROR;
		}
    	
		if (!valid) {
            // ProcessManager.saveProcess(p);
			String processlog = "Validation issues during in depth data analysis: " + "<br/>";
        	processlog += "<ul>";
            for (String s : issues) {
            	Helper.setFehlerMeldung(s);
    			processlog += "<li>" + s + "</li>";
            }
            processlog += "</ul>";
            Helper.addMessageToProcessLog(process.getId(),LogType.ERROR,processlog);
			step.setBearbeitungsstatusEnum(StepStatus.ERROR);
			try {
				StepManager.saveStep(step);
			} catch (DAOException e) {
		          log.error("Error while saving the step status.", e);
		    }
			return PluginReturnValue.ERROR;
		} else {
			Helper.addMessageToProcessLog(process.getId(),LogType.INFO,"The validation of all metadata fields was successfull without any issues.");
			return PluginReturnValue.FINISH;
		}
    }
    
    /**
     * generate metadata objects out of xml configuration
     * 
     * @param md the configuration to use
     */
    private MetadataMappingObject getMetadata(HierarchicalConfiguration md) {
        String rulesetName = md.getString("@ugh");
        String propertyName = md.getString("@name");
        Integer columnNumber = md.getInteger("@column", null);
        String headerName = md.getString("@headerName", null);
        String normdataHeaderName = md.getString("@normdataHeaderName", null);
        String docType = md.getString("@docType", "child");
        boolean required = md.getBoolean("@required", false);
        String patternString = md.getString("@pattern", "");
        String eitherHeader = md.getString("@either", "");
        String requiredFields = md.getString("@requiredFields", "");
        String listPath = md.getString("@list");
        String identifier = md.getString("@identifier");
        Integer wordcount = md.getInteger("@wordcount", 0);

        String requiredErrorMessage = md.getString("@requiredErrorMessage", "");
        String patternErrorMessage = md.getString("@patternErrorMessage", "");
        String validContentErrorMessage = md.getString("@listErrorMessage", "");
        String eitherErrorMessage = md.getString("@eitherErrorMessage", "");
        String requiredHeadersErrormessage = md.getString("@requiredFieldsErrormessage", "");
        String wordcountErrormessage = md.getString("@wordcountErrorMessage", "");
        
        ArrayList<String> validContent = new ArrayList<>();

        if (listPath != null && !listPath.isEmpty()) {
            try {
                validContent = readFileToList(listPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        String[] requiredHeaders = null;
        if (requiredFields != null) {
            requiredHeaders = requiredFields.split("; ");
        }
        Pattern pattern = null;
        if (!patternString.isEmpty()) {
            pattern = Pattern.compile(patternString);
        }
        
        MetadataMappingObject mmo = new MetadataMappingObject();
        mmo.setExcelColumn(columnNumber);
        mmo.setPropertyName(propertyName);
        mmo.setRulesetName(rulesetName);
        mmo.setHeaderName(headerName);
        mmo.setIdentifier(identifier);
        mmo.setNormdataHeaderName(normdataHeaderName);
        mmo.setDocType(docType);
        mmo.setRequired(required);
        mmo.setPattern(pattern);
        mmo.setValidContent(validContent);
        mmo.setEitherHeader(eitherHeader);
        mmo.setWordcount(wordcount);
        mmo.setEitherErrorMessage(eitherErrorMessage);
        mmo.setPatternErrorMessage(patternErrorMessage);
        mmo.setRequiredErrorMessage(requiredErrorMessage);
        mmo.setRequiredHeadersErrormessage(requiredHeadersErrormessage);
        mmo.setValidContentErrorMessage(validContentErrorMessage);
        mmo.setWordcountErrormessage(wordcountErrormessage);
        if (requiredHeaders != null) {
            mmo.setRequiredHeaders(requiredHeaders);
        }
        return mmo;
    }
    
    /**
     * read separate configuration file for validation
     * 
     * @param listPath file path for config file
     */
    private ArrayList<String> readFileToList(String listPath) throws FileNotFoundException {
        Scanner s = new Scanner(new File(listPath));
        ArrayList<String> validContent = new ArrayList<>();
        while (s.hasNext()) {
            validContent.add(s.next());
        }
        s.close();
        return validContent;
    }
    
    @Override
    public boolean execute() {
        PluginReturnValue val = run();
        if (val == PluginReturnValue.FINISH) {
            return true;
        }
        return false;
    }

    @Override
    public String cancel() {
        return null;
    }

    @Override
    public String finish() {
        return null;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public int getInterfaceVersion() {
        return 1;
    }
    
}
