package org.apache.maven.model.profile.activation;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.ProfileActivationException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;

/**
 * Determines profile activation based on the existence or value of some execution property.
 * 
 * @author Benjamin Bentmann
 */
@Component( role = ProfileActivator.class, hint = "property" )
public class PropertyProfileActivator
    implements ProfileActivator
{

    public boolean isActive( Profile profile, ProfileActivationContext context )
        throws ProfileActivationException
    {
        boolean active = false;

        Activation activation = profile.getActivation();

        if ( activation != null )
        {
            ActivationProperty property = activation.getProperty();

            if ( property != null )
            {
                String name = property.getName();
                boolean reverseName = false;

                if ( name == null )
                {
                    throw new ProfileActivationException( "The property name is required to activate the profile "
                        + profile.getId(), profile );
                }

                if ( name.startsWith( "!" ) )
                {
                    reverseName = true;
                    name = name.substring( 1 );
                }

                String sysValue = context.getUserProperties().getProperty( name );
                if ( sysValue == null )
                {
                    sysValue = context.getSystemProperties().getProperty( name );
                }

                String propValue = property.getValue();
                if ( StringUtils.isNotEmpty( propValue ) )
                {
                    boolean reverseValue = false;
                    if ( propValue.startsWith( "!" ) )
                    {
                        reverseValue = true;
                        propValue = propValue.substring( 1 );
                    }

                    // we have a value, so it has to match the system value...
                    boolean result = propValue.equals( sysValue );

                    if ( reverseValue )
                    {
                        active = !result;
                    }
                    else
                    {
                        active = result;
                    }
                }
                else
                {
                    boolean result = StringUtils.isNotEmpty( sysValue );

                    if ( reverseName )
                    {
                        active = !result;
                    }
                    else
                    {
                        active = result;
                    }
                }
            }
        }

        return active;
    }

}