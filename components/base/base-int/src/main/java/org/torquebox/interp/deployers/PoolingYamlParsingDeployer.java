/* Copyright 2009 Red Hat, Inc. */

package org.torquebox.interp.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.mc.AttachmentUtils;

/**
 * <pre>
 * Stage: PARSE
 *    In: pooling.yml
 *   Out: PoolMetaData
 * </pre>
 * 
 * Parsing deployer for {@code pooling.yml}.
 * 
 * <p>
 * This deployer looks for metadata files named exactly {@code pooling.yml},
 * which is expected to be a YAML file describing the configuration of various
 * Ruby runtime interpreter pools.
 * </p>
 * 
 * <p>
 * The top-level of the YAML file should be a hash, with the pool identifier as
 * the key. The value of each map may be the strings {@code global} or
 * {@code shared}, or another hash specifying {@code min} and {@code max} values
 * for the pool size.
 * </p>
 * 
 * <pre>
 *   pool_one: global
 *   pool_two: shared
 *   pool_three:
 *     min: 5
 *     max: 25
 * </pre>
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 * 
 * @see PoolMetaData
 */
public class PoolingYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    /**
     * Construct.
     */
    public PoolingYamlParsingDeployer() {
        setSectionName( "pooling" );
        addOutput( PoolMetaData.class );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(VFSDeploymentUnit unit, Object dataObj) throws Exception {
        Map<String, Object> pooling = (Map<String, Object>) dataObj;

        if (pooling != null) {
            for (String name : pooling.keySet()) {

                Object pool = pooling.get( name );

                PoolMetaData poolMetaData = new PoolMetaData( name );

                if (pool instanceof Map) {
                    Map<String, Object> poolMap = (Map<String, Object>) pool;

                    if (poolMap.get( "min" ) != null) {
                        poolMetaData.setMinimumSize( ((Number) poolMap.get( "min" )).intValue() );
                    }

                    if (poolMap.get( "max" ) != null) {
                        poolMetaData.setMaximumSize( ((Number) poolMap.get( "max" )).intValue() );
                    }
                } else if (pool instanceof String) {
                    if (pool.toString().equals( "shared" )) {
                        poolMetaData.setShared();
                    } else if (pool.toString().equals( "global" )) {
                        poolMetaData.setGlobal();
                    }
                }
                log.info( "Configured Ruby runtime pool: " + poolMetaData );
                AttachmentUtils.multipleAttach( unit, poolMetaData, name );
            }
        }

    }

}