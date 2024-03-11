FROM debian:bullseye

RUN apt-get update && apt-get -y install \
	equivs \
	wget \
	unzip \
	xz-utils \
	ruby-hocon
RUN mkdir /e39
WORKDIR /e39
RUN wget -q https://downloads.raspberrypi.org/raspios_lite_arm64/images/raspios_lite_arm64-2023-12-11/2023-12-11-raspios-bookworm-arm64-lite.img.xz
ADD combine.sh \
	e39-deps.control \
	e39firstboot.service \
	configure.sh \
	install-e39.sh \
	*.deb \
	*.jar \
	*.conf \
	jwmrc \
	config_txt/config.400x234.menu.ntsc.shorter.txt \
	video_overlay/vga565.dtbo \
	image.conf \
	intermediate/boot/ssh \
	intermediate/boot/userconf.txt \
	intermediate/e39/version.conf \
	intermediate/e39/*.jar \
	intermediate/e39/username \
	intermediate/wifisetup.sh \
	./
CMD [ "./combine.sh" ]
