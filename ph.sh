#!/bin/bash
aws lambda invoke --function-name PAC-PE-BATCH-DELETE out --log-type Tail --query 'LogResult' --output text |  base64 -d
