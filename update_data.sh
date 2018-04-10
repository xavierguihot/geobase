#!/bin/sh

set -e

# This script updates geobase data from the opentraveldata github repo.
# When building the geobase jar, the opentraveldata data file is incorporated.

cd src/main/resources
rm -f optd_por_public.csv
wget "https://raw.githubusercontent.com/opentraveldata/opentraveldata/master/opentraveldata/optd_por_public.csv"
rm -f optd_airlines.csv
wget "https://raw.githubusercontent.com/opentraveldata/opentraveldata/master/opentraveldata/optd_airlines.csv"
cd ../../..
