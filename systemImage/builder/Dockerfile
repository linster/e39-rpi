FROM debian:bullseye

RUN apt-get update && apt-get -y install \
	equivs \
	wget \
	unzip
RUN mkdir /e39
WORKDIR /e39
RUN wget -q https://downloads.raspberrypi.org/raspios_lite_armhf/images/raspios_lite_armhf-2021-11-08/2021-10-30-raspios-bullseye-armhf-lite.zip
ADD packages .
ADD e39-deps.control .
ADD combine.sh .
CMD [ "./combine.sh" ]
