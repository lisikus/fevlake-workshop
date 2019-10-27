FROM nginx:1.12-alpine
MAINTAINER Lisa Postarnak
ENV MAINTAINER=''

WORKDIR /usr/share/nginx/html
COPY endpoint.html /usr/share/nginx/html

CMD cd /usr/share/nginx/html && sed -e s/NON_DEFINED_ENDPOINT/"$MAINTAINER"/ endpoint.html > index.html ; nginx -g 'daemon off;'
