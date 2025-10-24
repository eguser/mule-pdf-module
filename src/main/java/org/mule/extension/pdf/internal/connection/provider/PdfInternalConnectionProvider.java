package org.mule.extension.pdf.internal.connection.provider;

import org.mule.extension.pdf.internal.connection.PdfInternalConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;

public class PdfInternalConnectionProvider implements ConnectionProvider<PdfInternalConnection> {

    @Override
    public PdfInternalConnection connect() throws ConnectionException {
        return new PdfInternalConnection();
    }

    @Override
    public void disconnect(PdfInternalConnection arg0) {
        //Do Nothing
    }

    @Override
    public ConnectionValidationResult validate(PdfInternalConnection arg0) {
        return ConnectionValidationResult.success();
    }



}
