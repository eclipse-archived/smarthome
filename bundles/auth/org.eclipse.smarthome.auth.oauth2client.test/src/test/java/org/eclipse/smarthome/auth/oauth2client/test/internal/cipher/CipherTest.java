package org.eclipse.smarthome.auth.oauth2client.test.internal.cipher;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.smarthome.auth.oauth2client.internal.cipher.SymmetricKeyCipher;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Verify that the SymmetricKeyCipher is working properly.
 *
 * @author Gary Tse - Initial Contribution
 */
public class CipherTest {

    private static final String PLAIN_TEXT = "hello world";

    private SymmetricKeyCipher spySymmetricKeyCipher;

    @Before
    public void setUp() throws IOException, InvalidSyntaxException, NoSuchAlgorithmException {
        spySymmetricKeyCipher = spySymmetricKeyCipher();
    }

    @Test
    public void testEncDec() throws GeneralSecurityException {

        String cipherText = spySymmetricKeyCipher.encrypt(PLAIN_TEXT);
        assertNotNull("Cipher text should not be null", cipherText);
        assertNotEquals("Cipher text should not be the same as plaintext", PLAIN_TEXT, cipherText);

        String decryptedText = spySymmetricKeyCipher.decrypt(cipherText);
        assertNotNull("Decrypted text should not be null", decryptedText);
        assertEquals("Decrypted text should be same as before", PLAIN_TEXT, decryptedText);
    }

    private SymmetricKeyCipher spySymmetricKeyCipher()
            throws IOException, InvalidSyntaxException, NoSuchAlgorithmException {
        spySymmetricKeyCipher = spy(SymmetricKeyCipher.class);
        spySymmetricKeyCipher.setConfigurationAdmin(mockConfigurationAdmin());
        spySymmetricKeyCipher.activate(); // generate encryptoin key
        return spySymmetricKeyCipher;
    }

    private ConfigurationAdmin mockConfigurationAdmin() throws IOException {
        ConfigurationAdmin configurationAdmin = mock(ConfigurationAdmin.class);
        when(configurationAdmin.getConfiguration(anyString())).thenReturn(mockConfiguration());
        return configurationAdmin;
    }

    private Configuration mockConfiguration() {
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        Configuration configurationMock = mock(Configuration.class);
        when(configurationMock.getPid()).thenReturn("PID");
        when(configurationMock.getProperties()).thenReturn(properties);
        return configurationMock;
    }

}
