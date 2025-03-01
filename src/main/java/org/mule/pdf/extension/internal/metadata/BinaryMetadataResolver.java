package org.mule.pdf.extension.internal.metadata;

import org.mule.metadata.api.model.MetadataType;
import org.mule.pdf.extension.internal.PDFModuleExtension;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;

public class BinaryMetadataResolver implements InputTypeResolver<Void>, OutputTypeResolver<Void> {

    @Override
    public String getCategoryName() {
        return PDFModuleExtension.class.getName();
    }
    
    @Override
    public String getResolverName() {
        return BinaryMetadataResolver.class.getName();
    }

    @Override
    public MetadataType getOutputType(MetadataContext context, Void key)
            throws MetadataResolvingException, ConnectionException {
        return context.getTypeBuilder().binaryType().build();
    }

    @Override
    public MetadataType getInputMetadata(MetadataContext context, Void key)
            throws MetadataResolvingException, ConnectionException {
        
        return context.getTypeBuilder().binaryType().build();
    }

}
