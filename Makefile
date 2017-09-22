all: build run
build: 
	javac src/*.java
	gunzip input/*
run:
	Rscript -e "rmarkdown::render('report.rmd')"
	gzip input/*
