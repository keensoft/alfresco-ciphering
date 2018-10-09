package es.keensoft.ciphering;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

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

public class EncipherActionExecuter extends ActionExecuterAbstractBase {
    
    public static final String ACTION_NAME = "keensoft-encipher-action";
    public static final String PARAM_PASSPHRASE = "passphrase";
    
    private static String PKCS5_ENC_PREFIX = "PKCS5-ENC-";
    private static String EXT = ".pkcs5";
    private static String MIME_TYPE = "application/pkcs5";

    private ServiceRegistry serviceRegistry;
    private CipheringHandler cipheringHandler;
    
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        
        try {
        
            String fileName = serviceRegistry.getNodeService().getProperty(actionedUponNodeRef,ContentModel.PROP_NAME).toString();
            File fileIn = TempFileProvider.createTempFile(fileName, PKCS5_ENC_PREFIX);
            ContentReader reader = serviceRegistry.getContentService().getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
            reader.getContent(fileIn);
            
            String passphrase = (String) action.getParameterValue(PARAM_PASSPHRASE);            
            CipherBean ci = cipheringHandler.getCipher(passphrase);
            
            File fileOut = TempFileProvider.createTempFile(fileName + EXT, PKCS5_ENC_PREFIX);
            FileOutputStream out = new FileOutputStream(fileOut);
            out.write(ci.getSalt());
            out.write(ci.getIv());
            
            try (FileInputStream in = new FileInputStream(fileIn)) {
                CipheringHandler.processFile(ci.getCipher(), in, out);
            }
            out.close();
            
            serviceRegistry.getNodeService().setProperty(actionedUponNodeRef,ContentModel.PROP_NAME, fileName + EXT);
            ContentWriter writer = serviceRegistry.getContentService().getWriter(actionedUponNodeRef,ContentModel.PROP_CONTENT,true);
            writer.setEncoding("UTF-8");
            writer.setMimetype(MIME_TYPE);
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
