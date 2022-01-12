FROM node:12-alpine

# be carefull changing this, it should change in Jenkinsfile as well
WORKDIR /usr/src/app

EXPOSE 3000

COPY package.json yarn.lock ./

RUN yarn install --production=false

COPY . .

CMD ["yarn", "start"]