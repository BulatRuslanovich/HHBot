# HHBot

## Table of Contents

- [About the Project](#about-the-project)
- [Architecture](#architecture)
- [Features](#features)
- [How It Works](#how-it-works)

## About the Project

HHBot is a Telegram bot designed for searching and notifying users about job vacancies on the HeadHunter platform. It is implemented using a microservices architecture with Spring Boot and integrates with Kafka, PostgreSQL, Telegram API, and HeadHunter API.

The project aims to automate the process of job search and notify users about suitable job offers. It provides convenient interaction through Telegram and supports filtering job vacancies based on various criteria.

## Architecture

- **Spring Boot**: Framework for creating web applications and microservices, offering flexibility and ease of development.
- **Kafka**: Messaging system for asynchronous communication between microservices, ensuring reliable message delivery.
- **ZooKeeper**: A distributed configuration management and coordination system for managing Kafka clusters and ensuring high availability.
- **PostgreSQL**: Relational database for storing user data and settings.
- **Redis**: An in-memory data store for caching and storing temporary data, ensuring lightning-fast performance and reduced latency.
- **Liquibase**: A database migration and version control tool for managing database schema changes and ensuring data consistency.
- **Telegram API**: Interface for interacting with Telegram and sending messages to users.
- **HeadHunter API**: Interface for retrieving job vacancies and searching for job offers on the HeadHunter platform.

## Features

- **Job Search**: The bot searches for job vacancies on the HeadHunter platform based on user-defined criteria.
- **Scheduled Notifications**: Users receive notifications about new job vacancies via Telegram according to their set schedule.
- **Experience Filtering**: Ability to filter job vacancies based on work experience level and other criteria.
- **Telegram Command Management**: Manage search queries and filtering options using commands in Telegram.

## How It Works

1. **Data Retrieval**: The bot periodically requests job vacancy data from the HeadHunter API based on user settings.
2. **Data Processing**: Retrieved job vacancies are processed and filtered according to user preferences and settings.
3. **Notification**: After processing, the bot sends notifications to users via Telegram about new job vacancies that match their interests.
