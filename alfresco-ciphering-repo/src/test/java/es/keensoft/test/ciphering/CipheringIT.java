package es.keensoft.test.ciphering;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.keensoft.ciphering.DecipherActionExecuter;
import es.keensoft.ciphering.EncipherActionExecuter;

@RunWith(value = AlfrescoTestRunner.class)
public class CipheringIT extends AbstractAlfrescoIT {

    // Sample data
    private static final String SAMPLE_PASSPHRASE = "keensoft";
    private static final String SAMPLE_FILE_NAME = "test.txt";
    private static final String SAMPLE_CONTENT = "Sample content";

    @SuppressWarnings("deprecation")
    @Test
    public void cipherAndDecipher() throws Exception {
        
        String adminUserName = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(adminUserName);
        RetryingTransactionHelper transactionHelper = 
                getServiceRegistry().getTransactionService().getRetryingTransactionHelper();
        
        // Create sample file in Shared folder
        final NodeRef sharedNodeRef = findNodeByPath("/app:company_home/app:shared");
        final NodeRef nodeRef = createContentNode(sharedNodeRef, SAMPLE_FILE_NAME, SAMPLE_CONTENT);
            
        try {
    
            // Cipher content action invocation
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                public Void execute() throws Throwable {
                    Action action = getServiceRegistry().getActionService().createAction(EncipherActionExecuter.ACTION_NAME);
                    action.setParameterValue(EncipherActionExecuter.PARAM_PASSPHRASE, SAMPLE_PASSPHRASE);
                    getServiceRegistry().getActionService().executeAction(action, nodeRef);
                    return null;
                }
            });
            
            // Ciphered content should be, at least, different from original content
            String cipheredContent = getServiceRegistry().getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT).getContentString();
            Assert.assertNotEquals(cipheredContent, SAMPLE_CONTENT);
            
            // Decipher content action invocation
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                public Void execute() throws Throwable {
                    Action action = getServiceRegistry().getActionService().createAction(DecipherActionExecuter.ACTION_NAME);
                    action.setParameterValue(DecipherActionExecuter.PARAM_PASSPHRASE, SAMPLE_PASSPHRASE);
                    getServiceRegistry().getActionService().executeAction(action, nodeRef);
                    return null;
                }
            });
            
            // Deciphered content must be equals to original content
            String plainContent = getServiceRegistry().getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT).getContentString();
            Assert.assertEquals(plainContent, SAMPLE_CONTENT);
        
        } finally {
        
            // Remove working file ever
            getServiceRegistry().getNodeService().deleteNode(nodeRef);
            
        }
        
    }
    
    private NodeRef findNodeByPath(String path) {
        Map<String,Serializable> params = new HashMap<>();
        params.put("query", path);
        return getServiceRegistry().getNodeLocatorService().getNode("xpath", null, params);
    }

    private NodeRef createContentNode(NodeRef parent, String name, String text) {

        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);

        NodeRef node = getServiceRegistry().getNodeService()
                .createNode(parent, ContentModel.ASSOC_CONTAINS, 
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                        ContentModel.TYPE_CONTENT, 
                        props)
                .getChildRef();

        ContentWriter writer = getServiceRegistry().getContentService().getWriter(node, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(text);

        return node;
    }
}
