#!/bin/sh

set -e

# This script updates geobase data with opentraveldata github repo

cd src/main/resources
rm -f optd_por_public.csv
wget "https://raw.githubusercontent.com/opentraveldata/opentraveldata/master/opentraveldata/optd_por_public.csv"
cd ../../..
