#!/bin/bash
aws lambda invoke --function-name PAC-PE-BATCH-INSERT-01 out --log-type Tail --query 'LogResult' --output text |  base64 -d
aws lambda invoke --function-name PAC-PE-BATCH-INSERT-02 out --log-type Tail --query 'LogResult' --output text |  base64 -d
