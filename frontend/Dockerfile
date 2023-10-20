FROM node:18-alpine
WORKDIR /home/app
COPY src /home/app/src
COPY public /home/app/public
COPY package.json /home/app
COPY package-lock.json /home/app
RUN npm install
CMD ["npm", "start"]
