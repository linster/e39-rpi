FROM debian:bullseye

RUN apt-get update && apt-get -y install \
	equivs \
	wget \
	unzip
RUN mkdir /e39
WORKDIR /e39
RUN wget -q https://downloads.raspberrypi.org/raspios_lite_arm64/images/raspios_lite_arm64-2021-11-08/2021-10-30-raspios-bullseye-arm64-lite.zip
ADD combine.sh \
	e39-deps.control \
	firstboot.service \
	configure.sh \
	install-e39.sh \
	*.deb \
	./
CMD [ "./combine.sh" ]
