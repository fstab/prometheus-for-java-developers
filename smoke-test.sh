for i in {1..100}
do
   echo  "Refresh $i times java app: \t" 
   curl http://localhost:8080/hello-world/
   echo "\n"
   sleep 3 
done
