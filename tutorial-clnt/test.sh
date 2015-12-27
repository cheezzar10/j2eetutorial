#!/bin/bash
id=1
while [ "$id" -lt 32 ]
do
	curl -X POST -d "userId=1&message=$id%3Acreating&message=$id%3Acreated&message=$id%3Astarting" http://localhost:8080/tutorial/notification/send
	((id += 1))
done