#!/bin/bash

# FIXME: Don't run Solr out of /tmp!
# Solr is /tmp to avoid AccessDeniedException under Minishift/OpenShift.
SOLR_DIR=/tmp/solr-7.3.0

if [ "$1" = 'solr' ]; then
    cd /tmp
    tar xvfz solr-7.3.0.tgz
    cp -r $SOLR_DIR/server/solr/configsets/_default $SOLR_DIR/server/solr/configsets/_default_dv
    cp /tmp/schema.xml $SOLR_DIR/server/solr/configsets/_default_dv
    cp /tmp/solrconfig.xml $SOLR_DIR/server/solr/configsets/_default_dv
    cd $SOLR_DIR
    bin/solr start
    bin/solr create_core -c core1 -d _default_dv
    sleep infinity
elif [ "$1" = 'usage' ]; then
    echo  'docker run -d iqss/dataverse-solr solr'
else
    exec "$@"
fi
