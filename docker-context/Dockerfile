# Backbone
FROM ruby:latest

# Maintainer infos.
MAINTAINER Daniel STANCU <birkof@birkof.ro>

# Dependecies installation and clean up.
RUN apt update && apt install -y nodejs && rm -rf /var/cache/apk/*

# Package installation.
RUN gem install redis-browser

# Expose ports.
EXPOSE 4567

# Define the entrypoint script.
ENTRYPOINT ["redis-browser"]
