from: java

ADD target/dist /appl

RUN rm -rf /appl/bin/start.bat
RUN chown 1000:1000 /appl/bin/start

WORKDIR /appl/bin
CMD [ "./start" ]