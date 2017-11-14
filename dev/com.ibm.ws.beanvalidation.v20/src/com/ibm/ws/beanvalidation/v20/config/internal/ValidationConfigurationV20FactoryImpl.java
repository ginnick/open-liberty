/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.beanvalidation.v20.config.internal;

import static org.osgi.service.component.annotations.ConfigurationPolicy.REQUIRE;

import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.ibm.ws.beanvalidation.config.ValidationConfigurationFactory;
import com.ibm.ws.beanvalidation.config.ValidationConfigurationInterface;
import com.ibm.ws.beanvalidation.service.BeanValidationContext;
import com.ibm.ws.beanvalidation.service.ValidationReleasableFactory;
import com.ibm.ws.beanvalidation.v20.config.ValidationConfiguratorV20;
import com.ibm.ws.javaee.dd.bval.ValidationConfig;
import com.ibm.wsspi.kernel.service.utils.AtomicServiceReference;

/**
 * This is the beanValidation-2.0 specific implementation. It allows us to return
 * a subclass configurator that knows about the v20 bval features. Use service.ranking
 * of 20 to represent the v.2.0 bval version.
 */
@Component(configurationPolicy = REQUIRE,
           service = ValidationConfigurationFactory.class,
           property = { "service.vendor=IBM", "service.ranking:Integer=20" })
public class ValidationConfigurationV20FactoryImpl implements ValidationConfigurationFactory {

    private static final String REFERENCE_VALIDATION_RELEASABLE_FACTORY = "ValidationReleasableFactory";

    private final AtomicServiceReference<ValidationReleasableFactory> validationReleasableFactorySR = new AtomicServiceReference<ValidationReleasableFactory>(REFERENCE_VALIDATION_RELEASABLE_FACTORY);

    @Override
    public ValidationConfigurationInterface createValidationConfiguration(BeanValidationContext context,
                                                                          ValidationConfig config) {
        return new ValidationConfiguratorV20(context, config, validationReleasableFactorySR.getService());
    }

    @Override
    public ConstraintValidatorFactory getConstraintValidatorFactoryOverride(Configuration<?> config) {
        ValidationConfiguratorV20 validationConfigurator = new ValidationConfiguratorV20(validationReleasableFactorySR.getService());
        return validationConfigurator.getConstraintValidatorFactoryOverride(config);
    }

    @Activate
    protected void activate(ComponentContext cc) {
        validationReleasableFactorySR.activate(cc);
    }

    @Deactivate
    protected void deactivate(ComponentContext cc) {
        validationReleasableFactorySR.deactivate(cc);
    }

    @Reference(name = REFERENCE_VALIDATION_RELEASABLE_FACTORY,
               service = ValidationReleasableFactory.class,
               cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.STATIC,
               policyOption = ReferencePolicyOption.GREEDY)
    protected void setValidationReleasableFactory(ServiceReference<ValidationReleasableFactory> factoryRef) {
        validationReleasableFactorySR.setReference(factoryRef);
    }

    protected void unsetValidationReleasableFactory(ServiceReference<ValidationReleasableFactory> factoryRef) {
        validationReleasableFactorySR.unsetReference(factoryRef);
    }

}
