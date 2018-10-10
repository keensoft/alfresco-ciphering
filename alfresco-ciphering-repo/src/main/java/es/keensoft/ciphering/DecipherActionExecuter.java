package es.keensoft.ciphering;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import javax.crypto.Cipher;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;

import es.keensoft.ciphering.util.CipheringHandler;

public class DecipherActionExecuter extends ActionExecuterAbstractBase {
    
    public static final String ACTION_NAME = "keensoft-decipher-action";
    public static final String PARAM_PASSPHRASE = "passphrase";
    
    private static String PKCS5_DEC_PREFIX = "PKCS5-DEC-";    
    
    private ServiceRegistry serviceRegistry;
    private CipheringHandler cipheringHandler;
    
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        
        try {
        
            String fileName = serviceRegistry.getNodeService().getProperty(actionedUponNodeRef, ContentModel.PROP_NAME).toString();
            File fileIn = TempFileProvider.createTempFile(fileName, PKCS5_DEC_PREFIX);
            ContentReader reader = serviceRegistry.getContentService().getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
            reader.getContent(fileIn);
            
            FileInputStream in = new FileInputStream(fileIn);
            byte[] salt = new byte[8], iv = new byte[128/8];
            in.read(salt);
            in.read(iv);
            
            String passphrase = (String) action.getParameterValue(PARAM_PASSPHRASE);
            Cipher ci = cipheringHandler.getDecipher(passphrase, salt, iv);
            
            String outputFileName = fileName.substring(0, fileName.lastIndexOf("."));
            File fileOut = TempFileProvider.createTempFile(outputFileName, PKCS5_DEC_PREFIX);
            try (FileOutputStream out = new FileOutputStream(fileOut)){
                CipheringHandler.processFile(ci, in, out);
            }
            
            serviceRegistry.getNodeService().setProperty(actionedUponNodeRef, ContentModel.PROP_NAME, outputFileName);
            ContentWriter writer = serviceRegistry.getContentService().getWriter(actionedUponNodeRef,ContentModel.PROP_CONTENT,true);
            writer.setMimetype(serviceRegistry.getMimetypeService().guessMimetype(outputFileName));
            writer.putContent(fileOut);
        
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_PASSPHRASE,
                DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_PASSPHRASE)));
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setCipheringHandler(CipheringHandler cipheringHandler) {
        this.cipheringHandler = cipheringHandler;
    }
    
}
