all: clean compile

compile:
	sh run.sh
clean:
	find . -name '*.class' -exec rm -f {} \;
