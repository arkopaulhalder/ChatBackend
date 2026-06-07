#!/bin/bash

echo "Firing 15 requests simultaneously..."

for i in {1..15}; do
  curl -s -o /dev/null -w "Request $i: HTTP %{http_code}\n" \
    -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"identifier":"testuser","password":"wrong"}' &
done

wait
echo "Done"