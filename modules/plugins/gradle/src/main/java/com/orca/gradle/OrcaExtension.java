package com.orca.gradle;

import javax.inject.Inject;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;

public class OrcaExtension {

    private final ConfigurableFileCollection sources;

    @Inject
    public OrcaExtension(ObjectFactory objects) {
        this.sources = objects.fileCollection();
    }

    public ConfigurableFileCollection getSources() {
        return sources;
    }
}
